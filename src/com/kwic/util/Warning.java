package com.kwic.util;

public class Warning extends Thread{
	public static final int ACTION_TWINKLE	= 1;
	public static final int ACTION_TYPE	= 2;
	public static final int ACTION_FLOW	= 3;
	public static final int ACTION_TYPEANDTWINKLE	= 4;
	
	private boolean stop;
	private String msg;
	private int dotCnt	= 3;
	private int action	= ACTION_TWINKLE;
	
	public Warning(String msg){
		this.action	= ACTION_TWINKLE;
		this.msg	= msg;
	}
	
	public Warning(String msg,int action){
		this.action	= action;
		this.msg	= msg;
	}
	
	public void run(){
		if(action==ACTION_TWINKLE)
			while(!stop)
				twinkle();
		else if(action==ACTION_TYPE)
			while(!stop)
				type();
		else if(action==ACTION_FLOW)
			flow();
		else if(action==ACTION_TYPEANDTWINKLE)
			while(!stop){
				type();
				twinkle();
				twinkle();
				twinkle();
			}
	}
	
	public void exit(){
		stop	= true;
	}
	
	public void twinkle(){
		int speed	= 500;
		try{
			Thread.sleep(speed);
			System.out.print("\r");
			System.out.print(msg);
			
			Thread.sleep(speed);
			System.out.print("\r");
			for(int i=0;i<msg.getBytes().length;i++){
				System.out.print(" ");
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	
	public void type(){
		int speed	= 500;
		try{
			Thread.sleep(speed);
			System.out.print("\r");
			for(int i=0;i<msg.length();i++){
				Thread.sleep(50);
				System.out.print(msg.charAt(i));
			}
			for(int i=0;i<dotCnt;i++){
				Thread.sleep(speed);
				System.out.print(".");
			}
			Thread.sleep(speed);
			System.out.print("\r");
			for(int i=0;i<msg.getBytes().length+dotCnt;i++){
				System.out.print(" ");
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public void flow(){
		int speed	= 200;
		String line	= msg;
		for(int i=0;i<dotCnt;i++){
			line	+= " ";
		}
		try{
			int idx	= 0;
			
			for(int i=0;i<line.length();i++){
				Thread.sleep(speed/2);
				System.out.print(line.charAt(i));
			}
			
			while(!stop){
				Thread.sleep(speed);
				if(idx>=line.length())
					idx	= 0;
				
				System.out.print("\r");
				System.out.print(line.substring(idx));
				if(idx!=0)
					System.out.print(line.substring(0,idx));
				
				idx++;
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	
	
}


