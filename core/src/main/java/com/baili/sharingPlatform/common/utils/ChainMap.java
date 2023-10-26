package com.baili.sharingPlatform.common.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * 使用链式编程模式对HashMap进行扩展，调用add、addAll方法返回当前Map对象
 * 示例:
 * <code>ChainMap.of("id","123").add("username", "admin").addAll(map)</code>
 *
 * @author baili
 * @date 2020/10/23.
 */
public class ChainMap<K, V> extends HashMap<K, V> {

	public ChainMap(int initialCapacity, float loadFactor) {
		super(initialCapacity, loadFactor);
	}

	public ChainMap(int initialCapacity) {
		super(initialCapacity);
	}

	public ChainMap() {
	}

	public ChainMap(Map<? extends K, ? extends V> m) {
		super(m);
	}

	public ChainMap(K key, V value) {
		put(key, value);
	}

	public static <K, V> ChainMap<K, V> of(K key, V value) {
		return new ChainMap<>(key, value);
	}

	public ChainMap<K, V> add(K key, V value) {
		put(key, value);
		return this;
	}

	public ChainMap<K, V> addAll(Map<? extends K, ? extends V> m) {
		putAll(m);
		return this;
	}

}
