package com.pk.template;

import com.pk.si.WsdlProperties;

public class HelperSource {

	public CharSequence firstLetterSmall(String word) {
		char c[] = word.toCharArray();
		c[0] = Character.toLowerCase(c[0]);
		return new String(c);
	}

	public CharSequence firstLetterSmallop(WsdlProperties word) {
		char c[] = word.getOperationName().toCharArray();
		c[0] = Character.toLowerCase(c[0]);
		return new String(c);
	}

	public CharSequence firstLetterSmallInput(WsdlProperties word) {
		char c[] = word.getInputParam().toCharArray();
		c[0] = Character.toLowerCase(c[0]);
		return new String(c);
	}
	
	public CharSequence firstLetterSmallReturn(WsdlProperties word) {
		char c[] = word.getReturnType().toCharArray();
		c[0] = Character.toLowerCase(c[0]);
		return new String(c);
	}

}
