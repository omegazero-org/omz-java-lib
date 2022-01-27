/*
 * Copyright (C) 2021 omegazero.org
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * Covered Software is provided under this License on an "as is" basis, without warranty of any kind,
 * either expressed, implied, or statutory, including, without limitation, warranties that the Covered Software
 * is free of defects, merchantable, fit for a particular purpose or non-infringing.
 * The entire risk as to the quality and performance of the Covered Software is with You.
 */
package org.omegazero.common.util;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Args implements Serializable {

	private static final long serialVersionUID = 1L;

	private final Map<String, Object> arguments = new HashMap<>();

	@Deprecated
	public Args() {
	}


	public void parseArguments(String[] args) {
		for(int i = 0; i < args.length; i++){
			String arg = args[i];
			if(arg.startsWith("--") && i + 1 < args.length){
				String key = arg.substring(2);
				put(key, args[i + 1]);
				i++;
			}else if(arg.startsWith("-")){
				String key = arg.substring(1);
				this.arguments.put(key, true);
			}
		}
	}


	public String getValue(String key) {
		Object v = this.arguments.get(key);
		if(v != null)
			return v.toString();
		else
			return null;
	}

	public String getValueOrDefault(String key, String def) {
		Object val = this.arguments.get(key);
		if(val != null)
			return val.toString();
		else
			return def;
	}

	public int getIntOrDefault(String key, int def) {
		return (int) this.getLongOrDefault(key, def);
	}

	public long getLongOrDefault(String key, long def) {
		Object val = this.arguments.get(key);
		if(val instanceof Number)
			return ((Number) val).longValue();
		else
			return def;
	}

	public float getFloatOrDefault(String key, float def) {
		return (float) this.getDoubleOrDefault(key, def);
	}

	public double getDoubleOrDefault(String key, double def) {
		Object val = this.arguments.get(key);
		if(val instanceof Number)
			return ((Number) val).doubleValue();
		else
			return def;
	}

	public boolean getBooleanOrDefault(String key, boolean def) {
		Object val = this.arguments.get(key);
		if(val instanceof Boolean)
			return (boolean) val;
		else
			return def;
	}


	private void put(String key, String value) {
		if(value.matches("-?\\d+")){
			this.arguments.put(key, Long.parseLong(value));
		}else if(value.matches("-?((\\d+)|(\\d*\\.\\d+)|(\\d+\\.\\d*))")){
			this.arguments.put(key, Double.parseDouble(value));
		}else if(value.equals("true")){
			this.arguments.put(key, true);
		}else if(value.equals("false")){
			this.arguments.put(key, false);
		}else{
			this.arguments.put(key, value);
		}
	}


	public static Args parse(String[] args) {
		Args a = new Args();
		a.parseArguments(args);
		return a;
	}
}
