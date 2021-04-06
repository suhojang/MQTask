package com.kwic.math;

import java.io.IOException;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.math.BigDecimal;

/**
 * <pre>
 * Title		: Calculator
 * Description	: 문자열로 작성된 수식의 계산
 * Date			: 
 * Copyright	: Copyright	(c)	2012
 * Company		: KWIC.
 * 
 *    수정일                  수정자                      수정내용
 * -------------------------------------------
 * 
 * </pre>
 *
 * @author 장정훈
 * @version	
 * @since 
 */
public class Calculator {
	
	/**<pre>
	 * 수식을 계산한 결과값을 반환한다.
	 * 
	 * [operator] 
	 * '+' : 뎃셈
	 * '-' : 뺄셈
	 * '/' : 나눗셈
	 * '*' : 곱셈
	 * '^' : 승
	 * ()  : 우선연산
	 * 
	 * [연산자우선순위]
	 * '()' --> '^' --> '*' --> '/' --> '+' --> '-'  
	 * 
	 * [함수]
	 * 'sqrt' : 제곱근 
	 * 'log'
	 * 'sin'
	 * 'asin'
	 * 'cos'
	 * 'acos'
	 * 'tan'
	 * 'atan'
	 * 'abs' : 절대값
	 * 'exp' : 율러함수
	 * 'ceil' : 올림
	 * 'round' : 반올림
	 * 'floor' : 내림
	 * 'rint' : 소숫점버림
	 * 'radian'
	 * 'degree'
	 * 'square' : 제곱 = ^2
	 * 'scalehalfdown[n] = scale[n]' : 소숫점자릿수설정 (ex: scale2(xxxx) - 소숫점2자리 반올림)
	 * 'scaledown[n]' : 소숫점자릿수설정 (ex: scaledown2(xxxx) - 소숫점2자리 내림)
	 * 'scaleup[n]' : 소숫점자릿수설정 (ex: scaleup2(xxxx) - 소숫점2자리 올림)
	 * 'scalehalfup[n]' : 소숫점자릿수설정 (ex: scalehalfup2(xxxx) - 소숫점2자리 반올림)
	 * </pre>
	 * @param expression String
	 * @return double
	 * @throws CrsException 
	 * */
	public static double calculate(String expression) throws Exception{
		StreamTokenizer stex;

		expression	= replaceAll(expression,"-","+(0-1)*");
		expression	= replaceAll(expression,"(+", "(");
		if(expression.startsWith("+"))
			expression	= expression.substring(1);
		
		double value	= 0;
		stex	= new StreamTokenizer(new StringReader(expression));
		stex.eolIsSignificant(true);
		stex.ordinaryChar('^');
		stex.ordinaryChar('/');
		stex.ordinaryChar('-');
		stex.ordinaryChar('*');
		
		stex.nextToken();
		
		value	= parse(stex);
		
		return value;
	}
	
	private static double parse(StreamTokenizer stex) throws Exception{
		double larg, rarg;
		larg	= term(stex);
		
		switch(stex.ttype){
			case '+':
				stex.nextToken();
				rarg	= parse(stex);		
				larg	= larg+rarg;	
				break;
			case '-':
				stex.nextToken();
				rarg	= parse(stex);		
				larg	= larg-rarg;	
				break;
			case ')':
			case StreamTokenizer.TT_EOF:
				break;
			default:
				error(stex);
		}
		return larg;
	}
	
	private static double term(StreamTokenizer stex) throws Exception{
		double larg, rarg;
		larg	= factor(stex);
		switch(stex.ttype){
			case '^':
				stex.nextToken();
				rarg	= term(stex);
				larg	= Math.pow(larg,rarg); 	
				break;
			case '*':
				stex.nextToken();
				rarg	= term(stex);		
				larg	= larg*rarg; 	
				break;
			case '/':
				stex.nextToken();
				rarg	= term(stex);		
				larg	= larg/rarg;	
				break;
			case '+':
			case '-':
			case ')':
			case StreamTokenizer.TT_EOF:
				break;
			default:
				error(stex);
		}
		return larg;
	}
	
	private static double factor(StreamTokenizer stex) throws Exception{
		double larg	= 0;
		switch(stex.ttype){
			case StreamTokenizer.TT_NUMBER:
				larg	= stex.nval;
				stex.nextToken();
				break;
			case '(':
				stex.nextToken();
				larg	= parse(stex);
				if(stex.ttype!=')')
					throw new IOException("부정확한 괄호 사용");
				stex.nextToken();
				break;
			case StreamTokenizer.TT_WORD:
				String funName	= stex.sval;
				stex.nextToken();
				if(stex.ttype!='(')
					throw new IOException("부정확한 함수 사용");
				stex.nextToken();
				double temp	= parse(stex);
				if(stex.ttype!=')')
					throw new IOException("부정확한 괄호 사용");
				if(funName.equalsIgnoreCase("sqrt")){
					larg	= Math.sqrt(temp);
				}else if(funName.equalsIgnoreCase("log")){
					larg	= Math.log(temp);
				}else if(funName.equalsIgnoreCase("sin")){
					larg	= Math.sin(temp);
				}else if(funName.equalsIgnoreCase("asin")){
					larg	= Math.asin(temp);
				}else if(funName.equalsIgnoreCase("cos")){
					larg	= Math.cos(temp);
				}else if(funName.equalsIgnoreCase("acos")){
					larg	= Math.acos(temp);
				}else if(funName.equalsIgnoreCase("tan")){
					larg	= Math.tan(temp);
				}else if(funName.equalsIgnoreCase("atan")){
					larg	= Math.atan(temp);
				}else if(funName.equalsIgnoreCase("abs")){
					larg	= Math.abs(temp);
				}else if(funName.equalsIgnoreCase("exp")){
					larg	= Math.exp(temp);
				}else if(funName.equalsIgnoreCase("ceil")){
					larg	= Math.ceil(temp);
				}else if(funName.equalsIgnoreCase("round")){
					larg	= Math.round(temp);
				}else if(funName.equalsIgnoreCase("floor")){
					larg	= Math.floor(temp);
				}else if(funName.equalsIgnoreCase("rint")){
					larg	= Math.rint(temp);
				}else if(funName.equalsIgnoreCase("radian")){
					larg	= Math.toRadians(temp);
				}else if(funName.equalsIgnoreCase("degree")){
					larg	= Math.toDegrees(temp);
				}else if(funName.equalsIgnoreCase("square")){
					larg	= Math.pow(temp,2);
				}else if(funName.toLowerCase().startsWith("scaleup")){
					int scale	= 0;
					if(funName.toLowerCase().equals("scaleup")){
						scale	= 0; 
					}else{
						scale	= Integer.parseInt(funName.toLowerCase().replaceAll("scaleup",""));
					}
					larg	= new BigDecimal(temp).setScale(scale,BigDecimal.ROUND_UP).doubleValue();
				}else if(funName.toLowerCase().startsWith("scaledown")){
					int scale	= 0;
					if(funName.toLowerCase().equals("scaledown")){
						scale	= 0; 
					}else{
						scale	= Integer.parseInt(funName.toLowerCase().replaceAll("scaledown",""));
					}
					larg	= new BigDecimal(temp).setScale(scale,BigDecimal.ROUND_DOWN).doubleValue();
				}else if(funName.toLowerCase().startsWith("scalehalfdown")){
					int scale	= 0;
					if(funName.toLowerCase().equals("scalehalfdown")){
						scale	= 0; 
					}else{
						scale	= Integer.parseInt(funName.toLowerCase().replaceAll("scalehalfdown",""));
					}
					larg		= new BigDecimal(temp).setScale(scale,BigDecimal.ROUND_HALF_DOWN).doubleValue();
				}else if(funName.toLowerCase().startsWith("scale")){
					int scale	= 0;
					if(funName.toLowerCase().equals("scale")){
						scale	= 0; 
					}else{
						scale	= Integer.parseInt(funName.toLowerCase().replaceAll("scale",""));
					}
					larg		= new BigDecimal(temp).setScale(scale,BigDecimal.ROUND_HALF_UP).doubleValue();
				}else{
					throw new IOException("부정확한 함수 사용");
				}
				stex.nextToken();
				break;
			default:
				error(stex);
		}
		return larg;
	}
	
	private static void error(StreamTokenizer stex) throws Exception{
		switch(stex.ttype){
			case StreamTokenizer.TT_NUMBER:
				throw new Exception("올바르지 않은 숫자 : "+stex.nval);
			case StreamTokenizer.TT_WORD:
				throw new Exception("올바르지 않은 문자 : "+stex.sval);
			default:
				throw new Exception("올바르지 않은 문자 : "+(char)stex.ttype);
		}
	}
	
    public static final String replaceAll(String source, String subject, String object) {
        StringBuffer rtnStr = new StringBuffer();
        String preStr		= "";
        String nextStr		= source;
        String srcStr		= source;

        while (srcStr.indexOf(subject) >= 0) {
            preStr	= srcStr.substring(0, srcStr.indexOf(subject));
            nextStr	= srcStr.substring(srcStr.indexOf(subject) + subject.length(), srcStr.length());
            srcStr	= nextStr;
            rtnStr.append(preStr).append(object);
        }
        rtnStr.append(nextStr);
        return rtnStr.toString();
    }

    public static void main(String[] args) throws Exception{
		String ex	= "1280801*0.012";
		double val	= Calculator.calculate(ex);
		System.out.println(ex+" = "+val);
		
		ex	= "scale3(1280801*0.012)";
		val	= Calculator.calculate(ex);
		if(val==new Double(val).intValue()){
			System.out.println(ex+" = "+new Double(val).intValue());
		}else{
			System.out.println(ex+" = "+val);
		}

		ex	= "scaleup3(1280801*0.012)";
		val	= Calculator.calculate(ex);
		if(val==new Double(val).intValue()){
			System.out.println(ex+" = "+new Double(val).intValue());
		}else{
			System.out.println(ex+" = "+val);
		}

		ex	= "scaledown3(1280801*0.012)";
		val	= Calculator.calculate(ex);
		if(val==new Double(val).intValue()){
			System.out.println(ex+" = "+new Double(val).intValue());
		}else{
			System.out.println(ex+" = "+val);
		}

		ex	= "2^(3+1)-2";
		val	= Calculator.calculate(ex);
		if(val==new Double(val).intValue()){
			System.out.println(ex+" = "+new Double(val).intValue());
		}else{
			System.out.println(ex+" = "+val);
		}
	}
}
