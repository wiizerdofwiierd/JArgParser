package org.wiizerdofwiierd.util.argparser;

import java.util.*;
import java.util.stream.Collectors;

public class ArgParser {

	private final TreeMap<String, Argument> validArgs = new TreeMap<>();

	/**
	 * Parses an array of Strings, grouping together words in quotation marks.<br>
	 * Incomplete quotations are parsed literally. For example, the arguments<br>
	 * <pre>
	 *     one" "two" "three
	 * </pre>
	 * would be parsed as
	 * <pre>
	 *     [one", two, "three]
	 * </pre>
	 * The following are some other examples:
	 * <br>
	 * <table border="1">
	 *     <tr>
	 *         <th>Given arguments</th>
	 *         <th>Result</th>
	 *     </tr>
	 *     <tr>
	 *         <td>one two three</td>
	 *         <td>[one, two, three]</td>
	 *     </tr>
	 *     <tr>
	 *         <td>"one two" "three"</td>
	 *         <td>[one two, three]</td>
	 *     </tr>
	 *     <tr>
	 *         <td>"one two" three</td>
	 *         <td>[one two, three]</td>
	 *     </tr>
	 *     <tr>
	 *         <td>one" "two three"</td>
	 *         <td>[one", two three]</td>
	 *     </tr>
	 *     <tr>
	 *         <td>"one "two "three</td>
	 *         <td>["one, "two, "three]</td>
	 *     </tr>
	 *     <tr>
	 *         <td>"one</td>
	 *         <td>["one]</td>
	 *     </tr>
	 *     <tr>
	 *         <td>one"</td>
	 *         <td>[one"]</td>
	 *     </tr>
	 *     <tr>
	 *         <td>one</td>
	 *         <td>[one]</td>
	 *     </tr>
	 * </table>
	 * @param args Arguments to parse
	 * @return A new array containing the parsed arguments
	 */
	public static String[] parseQuotes(String[] args){
		int length = 0;
		int quoted = 0;
		boolean counting = true;
		for(String s : args){
			if(s.startsWith("\"") && !s.endsWith("\"")){//Has begin quote
				length++;
				if(!counting){//If not currently counting each word (already had begin quote)
					length += quoted;
					quoted = 0;
				}
				counting = false;
			}
			else if(s.endsWith("\"") && !s.startsWith("\"")){//Has end quote
				if(counting) length++;
				quoted = 0;
				counting = true;
			}
			else if(counting) length++;//Has no quotes, not in quotes
			else quoted++;//Has no quotes, in quotes
		}
		if(!counting) length += quoted;

		String[] newArgs = new String[length];

		StringBuilder builder = new StringBuilder();
		int index = 0;
		int words = 0;
		boolean quotes = false;
		for(String s : args){
			if(s.startsWith("\"") && !s.endsWith("\"")){//Has begin quote
				if(quotes){
					if(builder.length() > 0){
						newArgs[index] = builder.toString();
						builder.setLength(0);
						index++;
					}
				}

				builder.append(s);
				words++;

				quotes = true;
			}
			else if(s.endsWith("\"") && !s.startsWith("\"")){//Has end quote
				if(words > 0){
					builder.append(" ");
				}
				builder.append(s);
				if(words > 0){
					String arg = builder.toString();
					newArgs[index] = arg.substring(1, arg.length()).substring(0, arg.length() - 2);//Remove begin and end quote
				}
				else newArgs[index] = builder.toString();

				builder.setLength(0);
				index++;
				words = 0;

				quotes = false;
			}
			else if(quotes){//In quotes
				builder.append(" ").append(s);
			}
			else{
				newArgs[index] = s;
				index++;
			}
		}
		if(quotes)
			for(String s : builder.toString().split(" ")){
				newArgs[index] = s;
				index++;
			}

		return newArgs;
	}
	
	/**
	 * Adds an additional argument to this parser. The parser will look for the argument when parsing.
	 * @param name Name of the argument as it should be in <code>args[]</code>, such as <i>-myvalue</i> or <i>--myflag</i>
	 * @param priority Priority of this argument. Arguments with a higher priority (lower number) are executed first
	 * @param argument Argument to be added to this parser
	 * @param required If <code>true</code>, this argument is required to exist for the program to continue running 
	 * @return This ArgumentParser, for chaining
	 * @see Argument
	 */
	public ArgParser withArgument(String name, int priority, Argument argument, boolean required){
		argument.priority = priority;
		argument.required = required;
		this.validArgs.put(name, argument);
		return this;
	}

	/**
	 * Adds an additional argument to this parser. The parser will look for the argument when parsing.
	 * @param name Name of the argument as it should be in <code>args[]</code>, such as <i>-myvalue</i> or <i>--myflag</i>
	 * @param priority Priority of this argument. Arguments with a higher priority (lower number) are executed first
	 * @param argument Argument to be added to this parser
	 * @return This ArgumentParser, for chaining
	 * @see Argument
	 */
	public ArgParser withArgument(String name, int priority, Argument argument){
		return withArgument(name, priority, argument, false);
	}

	/**
	 * Adds an additional argument to this parser. The parser will look for the argument when parsing.
	 * @param name Name of the argument as it should be in <code>args[]</code>, such as <i>-myvalue</i> or <i>--myflag</i>
	 * @param argument Argument to be added to this parser
	 * @param required If <code>true</code>, this argument is required to exist for the program to continue running    
	 * @return This ArgumentParser, for chaining
	 * @see Argument
	 */
	public ArgParser withArgument(String name, Argument argument, boolean required){
		return withArgument(name, validArgs.size(), argument, required);
	}

	/**
	 * Adds an additional argument to this parser. The parser will look for the argument when parsing.
	 * @param name Name of the argument as it should be in <code>args[]</code>, such as <i>-myvalue</i> or <i>--myflag</i>
	 * @param argument Argument to be added to this parser
	 * @return This ArgumentParser, for chaining
	 * @see Argument
	 */
	public ArgParser withArgument(String name, Argument argument){
		return withArgument(name, validArgs.size(), argument, false);
	}

	/**
	 * Parses an array of arguments, calling {@link Argument#handle(Object)} on each in order of priority
	 * @param args Arguments to parse
	 */
	public void parse(String[] args){
		Iterator<String> iterator = Arrays.stream(parseQuotes(args)).iterator();

		HashMap<Argument, Object> values = new HashMap<>();

		while(iterator.hasNext()){
			String s = iterator.next();
			if(validArgs.containsKey(s)){
				Argument argument = validArgs.get(s);
				values.put(argument, argument.parse(iterator));
			}
		}

		List<String> missingRequired = validArgs.keySet()
				.stream()
				.filter(a -> validArgs.get(a).required)
				.filter(a -> !values.containsKey(validArgs.get(a))).collect(Collectors.toList());

		if(missingRequired.size() > 0){
			System.err.println("Error when parsing arguments: One or more required arguments are missing:");

			for(String s : missingRequired){
				System.err.println("Missing: " + s);
			}

			System.exit(-1);
		}

		ParsedArgs arguments = new ParsedArgs(validArgs.size());

		for(Argument a : values.keySet().stream().sorted().collect(Collectors.toList())){//Sort based on priority and iterate
			a.handle(values.get(a));
		}
	}
	
	/**
	 * Represents a command-line argument
	 */
	public static abstract class Argument implements Comparable<Argument>{
		private int priority = 0;
		private boolean required = false;

		public abstract void handle(Object value);

		public abstract Object parse(Iterator<String> argsIterator);

		@Override
		public int compareTo(Argument other){
			return this.priority - other.priority;
		}
	}

	/**
	 * Represents a command-line argument flag. A flag is an argument that<br>
	 * represents a boolean value (<code>true</code> if the flag exists, otherwise <code>false</code>.)
	 */
	public static abstract class Flag extends Argument{
		@Override
		public Boolean parse(Iterator<String> argsIterator){
			return true;
		}
	}

	/**
	 * Represents a command-line argument with a value.<br>
	 * The value is always interpreted as a string.
	 */
	public static abstract class Value extends Argument{
		@Override
		public String parse(Iterator<String> argsIterator){
			if(argsIterator.hasNext())
				return argsIterator.next();
			else
				return null;
		}
	}
}
