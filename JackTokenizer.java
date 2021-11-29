import java.util.regex.*;
import java.util.*;

class JackTokenizer{
	public static final JackTokenizer instance = new JackTokenizer();

	private JackTokenizer(){}

	private Pattern keyWordPattern = Pattern.compile("class |constructor |function |method |field |static |var |int |char |boolean |void |true|false|null|this|let |do |if |else |while |return");
	private Pattern symbolPattern = Pattern.compile("\\{|\\}|\\(|\\)|\\[|\\]|\\.|\\,|;|\\+|-|\\*|/|&|\\||<|>|=|~");
	private Pattern integerConstantPattern = Pattern.compile("\\d+");
	private Pattern stringConstantPattern = Pattern.compile("\".*\"");
	private Pattern identifierPattern = Pattern.compile("[a-zA-Z_]+[a-zA-Z_0-9]*");
	private Pattern spacePattern = Pattern.compile("\\s|/\\*(.|[\r\n])*?\\*/|//.*\n");
	public String tokenize(String code){
		String s = "<tokens>\n";
		Matcher spaceMatcher = spacePattern.matcher(code);
		Matcher identifierMatcher = identifierPattern.matcher(code);
		Matcher stringConstantMatcher = stringConstantPattern.matcher(code);
		Matcher integerConstantMatcher = integerConstantPattern.matcher(code);
		Matcher symbolMatcher = symbolPattern.matcher(code);
		Matcher keyWordMatcher = keyWordPattern.matcher(code);
		for(int i=0;i<code.length();){
			if(spaceMatcher.find(i) && spaceMatcher.start()==i){
				i = spaceMatcher.end();
			}else if(keyWordMatcher.find(i) && keyWordMatcher.start()==i){
				s=s + "<keyword> "+code.substring(i,keyWordMatcher.end())+" </keyword>\n";
				i = keyWordMatcher.end();
			}else if(symbolMatcher.find(i) && symbolMatcher.start()==i){
				s = s + "<symbol> ";
				String symbol = code.substring(i,symbolMatcher.end());
				i = symbolMatcher.end();
				if(symbol.equals("<")){
					s+="&lt;";
				}else if(symbol.equals(">")){
					s+="&gt;";
				}else if(symbol.equals("&")){
					s+="&amp;";
				}else{
					s+=symbol;
				}
				s += " </symbol>\n";
			}else if(integerConstantMatcher.find(i) && integerConstantMatcher.start()==i){
				s = s + "<integerConstant> "+code.substring(i,integerConstantMatcher.end())+" </integerConstant>\n";
				i = integerConstantMatcher.end();
			}else if(stringConstantMatcher.find(i) && stringConstantMatcher.start()==i){
				s = s + "<stringConstant> "+code.substring(i+1,stringConstantMatcher.end()-1)+" </stringConstant>\n";
				i = stringConstantMatcher.end();
			}else if(identifierMatcher.find(i) && identifierMatcher.start()==i){
				s = s + "<identifier> "+code.substring(i,identifierMatcher.end())+" </identifier>\n";
				i = identifierMatcher.end();
			}else{
				System.out.println("Cannot match token to any type! "+code.substring(i));
				return "";
			}

		}
		s = s+ "</tokens>\n";
		return s;
	}
}