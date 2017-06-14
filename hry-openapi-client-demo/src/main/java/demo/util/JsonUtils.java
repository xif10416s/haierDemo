package demo.util;


import java.io.IOException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.*;

/**
 * JSON工具类
 * @author caobin
 * @version 1.0 2015年8月4日
 */
public class JsonUtils {
	
	/**
	 * Object转换为JSON字符串
	 * @Description
	 * @param obj 指定对象(POJO)
	 * @return
	 * @author caobin
	 */
	public static String obj2JsonString(Object obj){	
		try{
			ObjectMapper objectMapper = getObjectMapper();
			if(obj == null)return EMPTY_JSON_STRING;
			return objectMapper.writeValueAsString(obj);
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return EMPTY_JSON_STRING;
	}
	

	/**
	 * JSON字符串转换为Object
	 * @param strJsonString 需要转换的JSON字符串
	 * @param clazz
	 * @return
	 */
	public static<T> T jsonString2Object(String strJsonString, Class<T> clazz){
		try {
			ObjectMapper objectMapper = getObjectMapper();
			if(StringUtils.isBlank(strJsonString))strJsonString = EMPTY_JSON_STRING;
			return objectMapper.readValue(strJsonString, clazz);
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * @Description 获取ObjectMapper(单例，线程安全)
	 * @return
	 * @author caobin
	 */
	@SuppressWarnings("deprecation")
    public static ObjectMapper getObjectMapper(){
		if(objectMapper == null){
			objectMapperLock.lock();
			try{
				if(objectMapper == null){
					objectMapper = new ObjectMapper();
					/** 反序列化处理 **/
					objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
					//所有浮点型用BigDecimal处理
					objectMapper.enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS);
					/** 序列化处理 **/
					//科学计数法处理
					objectMapper.enable(SerializationFeature.WRITE_BIGDECIMAL_AS_PLAIN);
					//null字段忽略
					objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
					//为了签名验证,必须加入顺序配置
					objectMapper.configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true);
					objectMapper.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
				}
			}catch(Exception e){
				e.printStackTrace();
			}finally{
				objectMapperLock.unlock();
			}
		}
		return objectMapper;
	}
	

	private static Log log = LogFactory.getLog(JsonUtils.class);
	
	/**
	 * 空JSON字符串
	 */
	private final static String EMPTY_JSON_STRING = "{}";
	
	/**
	 * ObjectMapper
	 */
	private static volatile ObjectMapper objectMapper;
	
	/**
	 * ObjectMapper锁
	 */
	private static Lock objectMapperLock = new ReentrantLock();
}
