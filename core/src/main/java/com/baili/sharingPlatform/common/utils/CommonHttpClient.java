package com.baili.sharingPlatform.common.utils;

import com.dtflys.forest.Forest;
import com.dtflys.forest.http.ForestCookie;
import com.dtflys.forest.http.ForestRequest;
import com.dtflys.forest.http.ForestRequestType;
import com.dtflys.forest.http.ForestResponse;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author baili
 * @date 2022年06月13日5:02 下午
 */
@Slf4j
public class CommonHttpClient {
    private ForestRequest<?> request = Forest.request();

    public ForestCookie requestCookie(String url, Map<String, Object> map) {
        AtomicReference<ForestResponse> response = new AtomicReference<>(null);
        AtomicReference<ForestCookie> cookieAtomic = new AtomicReference<>(null);
        Object requestResult = null;
        requestResult = request.setType(ForestRequestType.POST).setContentType("application/json")
                .url(url).addBody(map).successWhen(((req, res) -> {
                    //获取响应结果
                    return res.noException() &&
                            res.statusOk() &&
                            res.statusIsNot(203);
                })).onSaveCookie(((forestRequest, forestCookies) -> {
                    log.info(forestCookies.allCookies().toString());
                    cookieAtomic.set(forestCookies.allCookies().get(0));
                })).execute();
        log.info( "请求地址：" + url + " 的响应结果为：" + requestResult.toString());
        return cookieAtomic.get();
    }

    public AtomicReference<ForestResponse> requestGet(String url, ForestCookie cookie) {
        AtomicReference<ForestResponse> response = new AtomicReference<>(null);
        Object requestResult = null;
        requestResult = request.setType(ForestRequestType.GET).url(url).successWhen(((req, res) -> {
                    //判断是否请求成功
                    return res.noException() &&
                            res.statusOk() &&
                            res.statusIsNot(203);
                })).onLoadCookie(((req, cookies) -> {
                    // req 为Forest请求对象，即 ForestRequest 类实例
                    // cookies 为Cookie集合, 需要通过请求发送的Cookie都添加到该集合
                    cookies.addCookie(cookie);
                }))
                .onSuccess(((data, req, res) -> {
                    // data 为响应成功后返回的反序列化过的数据
                    // req 为Forest请求对象，即 ForestRequest 类实例
                    // res 为Forest响应对象，即 ForestResponse 类实例
                    response.set(res);
                })).execute();
        log.info( "请求地址：" + url + " 的响应结果为：" + requestResult.toString());
        return response;
    }

    public AtomicReference<ForestResponse> requestPost(String url, Map<String, Object> map, ForestCookie cookie,
                                                       int timeOut) {
        AtomicReference<ForestResponse> response = new AtomicReference<>(null);
        Object requestResult = null;
        requestResult =
                request.setType(ForestRequestType.POST).url(url).setContentType("application/json").addBody(map).successWhen(((req,
                                                                                                             res) -> {
                    //获取响应结果
                    return res.noException() &&
                            res.statusOk() &&
                            res.statusIsNot(203);
                })).onLoadCookie(((req, cookies) -> {
                    // req 为Forest请求对象，即 ForestRequest 类实例
                    // cookies 为Cookie集合, 需要通过请求发送的Cookie都添加到该集合
                    cookies.addCookie(cookie);
                }))
                .onSuccess(((data, req, res) -> {
                    // data 为响应成功后返回的反序列化过的数据
                    // req 为Forest请求对象，即 ForestRequest 类实例
                    // res 为Forest响应对象，即 ForestResponse 类实例
                    response.set(res);
                })).execute();
        //log.info( "请求地址：" + url + " 的响应结果为：" + requestResult.toString());
        return response;
    }

    public AtomicReference<ForestResponse> requestPost(String url, List<Object> requestList, ForestCookie cookie,
                                                       int timeOut) {
        AtomicReference<ForestResponse> response = new AtomicReference<>(null);
        Object requestResult = null;
        requestResult =
                request.setType(ForestRequestType.POST).url(url).contentTypeJson().addBody(requestList).successWhen(((req,
                                                                                                                               res) -> {
                            //获取响应结果
                            return res.noException() &&
                                    res.statusOk() &&
                                    res.statusIsNot(203);
                        })).onLoadCookie(((req, cookies) -> {
                            // req 为Forest请求对象，即 ForestRequest 类实例
                            // cookies 为Cookie集合, 需要通过请求发送的Cookie都添加到该集合
                            cookies.addCookie(cookie);
                        }))
                        .onSuccess(((data, req, res) -> {
                            // data 为响应成功后返回的反序列化过的数据
                            // req 为Forest请求对象，即 ForestRequest 类实例
                            // res 为Forest响应对象，即 ForestResponse 类实例
                            response.set(res);
                        })).execute();
        //log.info( "请求地址：" + url + " 的响应结果为：" + requestResult.toString());
        return response;
    }

    public AtomicReference<ForestResponse> getUploadFile(String url, String name, File file,
                                                         ForestCookie cookie) {
        AtomicReference<ForestResponse> response = new AtomicReference<>(null);
        Object requestResult = null;
        requestResult = request.setType(ForestRequestType.POST).contentTypeMultipartFormData().addFile(name, file)
                .url(url).successWhen(((req, res) -> {
                    //获取响应结果
                    return res.noException() &&
                            res.statusOk() &&
                            res.statusIsNot(203);
                })).onLoadCookie(((req, cookies) -> {
                    // req 为Forest请求对象，即 ForestRequest 类实例
                    // cookies 为Cookie集合, 需要通过请求发送的Cookie都添加到该集合
                    cookies.addCookie(cookie);
                })).onProgress(forestProgress -> {
                    //System.out.println("-------------------------------------------------------");
                    //System.out.println("total bytes: " + forestProgress.getTotalBytes());
                    //System.out.println("current bytes: " + forestProgress.getCurrentBytes());
                    System.out.println("percentage: " + (forestProgress.getRate() * 100) + "%");
                    if (forestProgress.isDone()) {
                        // 若已传输完毕
                        System.out.println("文件传输完成");
                    }
                })
                .onSuccess(((data, req, res) -> {
                    // data 为响应成功后返回的反序列化过的数据
                    // req 为Forest请求对象，即 ForestRequest 类实例
                    // res 为Forest响应对象，即 ForestResponse 类实例
                    response.set(res);
                })).execute();
        log.info( "请求地址：" + url + " 的响应结果为：" + requestResult.toString());
        return response;
    }

}
