/* (c) 2017 Péter Varkoly <peter@varkoly.de> - all rights reserved */
package de.openschoolserver.dao.controller;
import java.util.*;
import java.io.IOException;
import java.nio.file.*;
import java.text.SimpleDateFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Config {

	Logger logger = LoggerFactory.getLogger(Config.class);
	private Path OSS_CONFIG = Paths.get("/etc/sysconfig/schoolserver");
	private String prefix = "SCHOOL_";
	private Map<String,String>   config;
	private Map<String,String>   configPath;
	private Map<String,Boolean>  readOnly;
	private List<String>         configFile;
	
	public Config() {
		this.InitConfig();
	}
	
	public Config(String configPath, String prefix) {
		this.OSS_CONFIG = Paths.get(configPath);
		this.prefix = prefix;
		this.InitConfig();
	}
	
	public void InitConfig() {
		config     = new HashMap<>();
		readOnly   = new HashMap<>();
		configPath = new HashMap<>();
		try {
			configFile = Files.readAllLines(this.OSS_CONFIG);
		}
		catch( IOException e ) { 
			e.printStackTrace();
		}
		Boolean ro = false;
		String  path = "Backup";
		for ( String line : configFile ){
			if( line.startsWith("#") && line.contains("readonly")) {
				ro = true;
			}
			if( line.startsWith("## Path:")) {
				String[] l = line.split("\\/");
				if( l.length == 3 )
				  path = l[2];
			}
			if( !line.startsWith("#") ) {
				String[] sline = line.split("=", 2);
				if( sline.length == 2 )
				{
					String key   = sline[0].substring(this.prefix.length());
					String value = sline[1];
					if( value.startsWith("\"") || value.startsWith("'") ){
						value = value.substring(1);
					}
					if( value.endsWith("\"") || value.endsWith("'") ){
						value = value.substring(0,value.length()-1);
					}
					readOnly.put(key,  ro);
					config.put(key,    value);
					configPath.put(key,path);
					ro = false;
				}
			}
		}
	}

	public Date now() {
		return new Date(System.currentTimeMillis());
	}

	public String nowString() {
		SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd.HH-mm-ss");
		return  fmt.format(new Date());
	}
	
	public Boolean isConfgiReadOnly(String key){
		return readOnly.get(key);
	}
	
	public String getConfigValue(String key){
		return config.get(key);
	}
	
	public String getConfigPath(String key){
		return configPath.get(key);
	}
	
	public List<String> getConfigPaths() {
 		List<String> paths = new ArrayList<String>();
		for ( String path : configPath.values() )
		{
		   if(!paths.contains(path))
			   paths.add(path);
		}
		return paths;
	}
	
	public List<String> getKeysOfPath(String path) {
		List<String> keys = new ArrayList<String>();
		for ( String key : configPath.keySet() ) {
			if( configPath.get(key).startsWith(path) )
			  keys.add(key);
		}
		Collections.sort(keys);
		return keys;
	}
	
	public Boolean setConfigValue(String key, String value){
		Boolean ro = readOnly.get(key);
		if(ro!=null && ro.booleanValue()){
			return false;
		}
		config.put(key, value);
		List<String> tmpConfig =  new ArrayList<String>();
		for ( String line : configFile ){
			if(line.startsWith(this.prefix + key)){
				tmpConfig.add( this.prefix + key + "=\"" + value + "\"" );  
			}
			else{
				tmpConfig.add( line );
			}
		}
		configFile = tmpConfig;
		try {
			Files.write(this.OSS_CONFIG, tmpConfig );
		}
		catch( IOException e ) { 
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	public List<Map<String,String>> getConfig() {
		List<Map<String, String>> configs = new ArrayList<>();
		for( String key : config.keySet() ){
			Map<String,String> configMap = new HashMap<>();
			configMap.put("key",      key);
			configMap.put("path",     configPath.get(key));
			configMap.put("value",    config.get(key));
			configMap.put("readOnly", readOnly.get(key) ? "yes" : "no" );
			configs.add(configMap);
		}
		return configs;
	}
}
