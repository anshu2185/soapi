package com.pk.template;


public class HelperSource {

	public CharSequence firstLetterSmall( String word) {
		char c[] = word.toCharArray();
		c[0] = Character.toLowerCase(c[0]);
		  return new String(c);
		}
}
