package com.baili.sharingPlatform.web.controller;

import com.baili.sharingPlatform.api.web.ResponseEntity;
import com.baili.sharingPlatform.api.web.requestModel.ApolloConfigModel.ApolloConfig;
import com.baili.sharingPlatform.api.web.requestModel.ProductInstallMessage;
import com.baili.sharingPlatform.model.installationTools.enums.InstallState;
import com.baili.sharingPlatform.model.installationTools.enums.ProductType;
import com.baili.sharingPlatform.service.ServiceApolloClientHelper;
import com.baili.sharingPlatform.service.ServiceProductInstall;
import com.baili.sharingPlatform.service.ServiceStressTestingToolsInit.ServiceVFourInit;
import com.baili.sharingPlatform.service.ServiceInstallationToolsDeploy;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ObjectUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

import static com.baili.sharingPlatform.common.utils.VersionUtils.interceptVersion;
import static com.baili.sharingPlatform.common.utils.VersionUtils.lessThan;

/**
 * @author baili
 * @date 2022年08月18日11:00 PM
 */
@Api(tags = "TestOps平台部署平台部署接口")
@Slf4j
@Validated
@RestController
@RequestMapping("/productAutoInstall")
public class ProductInstallController {
    Logger logger = LoggerFactory.getLogger(RemoteDeploymentController.class);

    @Resource
    private ServiceInstallationToolsDeploy serviceInstallationToolsDeploy;
    @Resource
    private ServiceProductInstall serviceProductInstall;
    @Resource
    private ServiceApolloClientHelper serviceApolloClientHelper;
    @Resource
    private ServiceVFourInit  serviceVFourInit;

    @ApiOperation(value = "一键安装产品")
    @PostMapping("/oneKeySetup")
    public ResponseEntity productImport(@Valid @RequestBody ProductInstallMessage productInstallMessage) throws InterruptedException {

        //初始化部署平台地址
        serviceProductInstall.initInstallationToolsUrl(productInstallMessage.getInstallationToolsHostIp());

        //判断参数传入的是否是已安装部署平台
        if (productInstallMessage.isInstallationToolsInstalled()) {
            //判断部署平台是否已安装
            if (!serviceProductInstall.login(productInstallMessage.getLoginMessage())) {
                return ResponseEntity.fail("部署平台未安装，请调整请求参数：installationToolsInstalled为false，或手动安装部署平台重新请求接口");
            }
        }
        //安装部署平台
        if (serviceInstallationToolsDeploy.deployInstallationTools(productInstallMessage.getDeployInstallationTools().getServerConfig(),
                productInstallMessage.getDeployInstallationTools().getWgetConfig(),
                productInstallMessage.getDeployInstallationTools().getInstallationToolsConfig())) {

            log.info("安装部署平台到" + productInstallMessage.getDeployInstallationTools().getServerConfig().getHost() +
                    "的目录" + productInstallMessage.getDeployInstallationTools().getWgetConfig().getInstallDir() + "成功");
        } else {
            return ResponseEntity.fail("部署部署平台失败，请检查服务器信息或下载源是否有效");
        }


        //登录
        if (!serviceProductInstall.login(productInstallMessage.getLoginMessage())) {
            return ResponseEntity.fail("登录失败，请检查用户名密码,或检查部署平台是否启动完成");
        }
        //如果是产品集导入阶段则，导入产品集
        if (StringUtils.equals(serviceProductInstall.productInstallStage(), InstallState.ProductSetInstall.getValue())) {
            //获取产品集列表
            String productName = null;
            productName = serviceProductInstall.productNameActive(productInstallMessage.getProductName());
            if (StringUtils.isEmpty(productName)) {
                return ResponseEntity.fail("获取产品集信息失败，请检查指定安装是否上传产品集是否存在或是否有且只有一个产品集或指定的产品集名称与上传的名称是否一致");
            }
            //导入产品集
            System.out.println(productName);
            if (!serviceProductInstall.productImport(productName)) {
                return ResponseEntity.fail("上传产品集失败，请重新上传");
            }
        }
        //如果是服务器添加阶段则添加服务器
        if (StringUtils.equals(serviceProductInstall.productInstallStage(), InstallState.ServerConfigure.getValue())) {
            //添加服务器
            if (!serviceProductInstall.productServerConfigure(productInstallMessage.getServerHostMessage())) {
                return ResponseEntity.fail("添加服务器失败，请检查服务器配置");
            }
        }
        //如果是部署方案配置阶段则部署配置
        if (StringUtils.equals(serviceProductInstall.productInstallStage(), InstallState.DeploymentConfigure.getValue())) {
            //单机部署方案配置
            if (!serviceProductInstall.productConfigureStandalone()) {
                return ResponseEntity.fail("单机部署方案配置失败，请检查配置");
            }
        }

        //如果是基础服务安装阶段则安装基础服务
        if (StringUtils.equals(serviceProductInstall.productInstallStage(), InstallState.BasicServiceInstall.getValue())) {
            //基础服务部署
            if (!serviceProductInstall.productBasicServiceInstall()) {
                return ResponseEntity.fail("基础服务安装失败，请检查");
            }
        }

        //如果是数据初始化阶段则一键执行数据初始化
        if (StringUtils.equals(serviceProductInstall.productInstallStage(), InstallState.DataInit.getValue())) {
            //数据初始化
            if (!serviceProductInstall.productDataInitialization()) {
                return ResponseEntity.fail("数据初始化执行失败，请检查");
            }
        }

        //如果是上传license阶段则上传license并进入到下一步
        if (StringUtils.equals(serviceProductInstall.productInstallStage(), InstallState.LicenseImport.getValue())) {
            if (!serviceProductInstall.productLiscenseFileUpload()) {
                return ResponseEntity.fail("license文件上传失败，请检查");
            }
        }

        //如果是平台应用安装阶段，则部署平台应用
        if (StringUtils.equals(serviceProductInstall.productInstallStage(), InstallState.PlatformServiceInstall.getValue())) {
            if (!serviceProductInstall.productPlatformServiceInstall()) {
                return ResponseEntity.fail("平台应用安装失败，请检查");
            }
        }
       //如果安装的是stressTestingToolsV4的产品集，则需要执行初始化操作；
        //初始化xxljob 的sql
        if (StringUtils.contains(productInstallMessage.getDeployInstallationTools().getInstallationToolsConfig().getProbePackageUrl(), ProductType.StressTestingToolsV4.getValue())){
            //执行xxljob
            if(!serviceVFourInit.initStressTestingToolsXxljobSql(productInstallMessage.getDeployInstallationTools().getServerConfig().getHost())){
                return ResponseEntity.fail("平台stressTestingTools  xxljob初始化sql执行失败，请手动处理sql以及请求stressTestingTools初始化接口");
            }
            try {
                //判断是否低于4.7
                Boolean isLowVersion = lessThan(interceptVersion(productInstallMessage.getDeployInstallationTools().getInstallationToolsConfig().getProbePackageUrl()), ProductType.StressTestingToolsV4Low.getValue());
                serviceVFourInit.initStressTestingToolsRequest(productInstallMessage.getDeployInstallationTools().getServerConfig().getHost(), isLowVersion);
            }catch(Exception e){
                e.printStackTrace();
                return ResponseEntity.fail("平台stressTestingTools 请求stressTestingTools初始化接口失败，请手动执行");
            }
        }

        return ResponseEntity.ok("部署平台安装部署完成，访问地址：http://" + productInstallMessage.getInstallationToolsHostIp() + ":8080");
    }


    //修改apollo配置
    @ApiOperation(value = "向apollo中新增配置项并发布")
    @PostMapping("/updateIncrConfig")
    public String apolloConfig(@Valid @RequestBody ApolloConfig apolloConfig) {
        if (apolloConfig.getApolloLoginMessage().getUserName().isEmpty()) {
            return "请填写apollo登录用户名";
        }
        if (apolloConfig.getApolloLoginMessage().getPassWord().isEmpty()) {
            return "请填写apollo登录密码";
        }
        if (apolloConfig.getApolloLoginMessage().getApolloAddress().isEmpty()) {
            return "请填写apollo地址，示例：10.10.227.18:8503";
        }
        serviceApolloClientHelper.setApolloUrl(apolloConfig.getApolloLoginMessage().getApolloAddress(), apolloConfig.getApolloLoginMessage().getUserName(), apolloConfig.getApolloLoginMessage().getPassWord());
        try {
            if (!serviceApolloClientHelper.getApps().contains(apolloConfig.getAppId())) {
                return "应用：" + apolloConfig.getAppId() + "不存在，请检查请求参数";
            }
            if (!serviceApolloClientHelper.getNamespaces(apolloConfig.getAppId()).contains(apolloConfig.getNamespace())) {
                return "namespace：" + apolloConfig.getNamespace() + "不存在，请检查请求参数";
            }
            serviceApolloClientHelper.updateIncrConfig(apolloConfig.getAppId(), apolloConfig.getNamespace(),
                    apolloConfig.getConfigs());
            if(ObjectUtils.isEmpty(apolloConfig.getNeedRestartApp())){
                return "[" + apolloConfig.getAppId() + "]的配置更新发布成功,不需要重启应用";
            }
            if (apolloConfig.getNeedRestartApp().isNeedRestartApp()) {
                if(ObjectUtils.isEmpty(apolloConfig.getNeedRestartApp().getDeployInstallationToolsAppMessage().getLoginMessage()) ||
                ObjectUtils.isEmpty(apolloConfig.getNeedRestartApp().getDeployInstallationToolsAppMessage().getInstallationToolsHostIp())){
                    return "请求参数中要求重启应用，但重启应用时部署平台信息填写错误，请手动重启应用";
                }
                serviceProductInstall.login(apolloConfig.getNeedRestartApp().getDeployInstallationToolsAppMessage().getLoginMessage());
                serviceProductInstall.initInstallationToolsUrl(apolloConfig.getNeedRestartApp().getDeployInstallationToolsAppMessage().getInstallationToolsHostIp());
                //TODO 目前只是默认重启需要添加参数的应用，如后续有多个关联应用需要重启再修改
                serviceProductInstall.deploySpecifiedService("service", apolloConfig.getAppId());
                return "[" + apolloConfig.getAppId() + "]的配置更新发布成功并重启应用：" + apolloConfig.getAppId() + "成功";
            }else {
                return "[" + apolloConfig.getAppId() + "]的配置更新发布成功,不需要重启应用";
            }
            //return "[" + apolloConfig.getAppId() + "]的配置更新发布成功，需要重启应用但未配置部署平台信息请手动重启应用";
        } catch (Exception e) {
            return "[" + apolloConfig.getAppId() + "]的配置更新发布失败，请查看日志";
        }


    }
    @ApiOperation(value = "向mysql中添加stressTestingTools需要的xxljob任务")
    @PostMapping("/xxlJobStressTestingToolsSql")
    public ResponseEntity xxlJobStressTestingToolsSql(@RequestParam(value = "productVersion", defaultValue = "4.7") String productVersion ,
                                        @RequestParam(value = "mysqlAddress", defaultValue = "10.10.x.x") String mysqlAddress){
        //如果安装的是stressTestingToolsV4的产品集，则需要执行初始化操作；
        //初始化xxljob 的sql
        if (StringUtils.contains(productVersion, ProductType.StressTestingToolsV4.getValue())){
            //执行xxljob
            if(!serviceVFourInit.initStressTestingToolsXxljobSql(mysqlAddress)){
                return ResponseEntity.fail("平台stressTestingTools V4 xxljob初始化sql执行失败，请手动处理sql");
            }
        }else{
            return ResponseEntity.ok("请求成功，高于V4版本的产品集不需要执行xxljob初始化sql");
        }
        return  ResponseEntity.ok("平台stressTestingTools V4 xxljob初始化sql执行成功");
    }

}
