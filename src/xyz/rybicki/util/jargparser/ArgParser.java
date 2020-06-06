package xyz.rybicki.util.jargparser;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ArgParser {

	private final TreeMap<String, Argument> validArgs = new TreeMap<>();

	/**
	 * Parses a String, returning an array of each token while preserving elements within quotation marks as a single token<br>
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
	 * @param input Input to parse
	 * @return An array containing the parsed arguments
	 */
	public static String[] parseQuotedArguments(String input){
		List<String> arguments = new ArrayList<>();

		Matcher matcher = Pattern
				.compile("(?:\"([^\"]+)\" ?|(\\S+))? ?+")
				.matcher(input);

		// TODO: Document this
		while(matcher.find()){
			if(matcher.group(1) != null)
				arguments.add(matcher.group(1));

			if(matcher.group(2) != null)
				arguments.add(matcher.group(2));
		}

		return arguments.toArray(new String[0]);
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
	 * @param input Entire input used to parse arguments for
	 */
	public void parse(String input){
		Iterator<String> iterator = Arrays.stream(parseQuotedArguments(input)).iterator();

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
	 * Parses an array of arguments, calling {@link Argument#handle(Object)} on each in order of priority
	 * @param args Arguments to parse
	 */
	public void parse(String[] args){
		parse(String.join(" ", args));
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
