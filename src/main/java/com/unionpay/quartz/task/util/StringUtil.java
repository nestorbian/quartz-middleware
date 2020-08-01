package com.unionpay.quartz.task.util;

import com.unionpay.quartz.task.exception.BizException;

public class StringUtil {
    public static boolean isBlank(CharSequence str) {
        return org.springframework.util.StringUtils.isEmpty(str);
    }
    
	public static void validateEmpty(String str, String msg) {
		if (StringUtil.isBlank(str)) {
			throw new BizException(msg);
		}
	}
}
