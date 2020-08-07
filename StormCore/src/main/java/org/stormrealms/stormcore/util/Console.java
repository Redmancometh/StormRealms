package org.stormrealms.stormcore.util;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.stereotype.Component;

@Component
public class Console {
	private PrintStream outStream = System.out;
	private PrintStream errStream = System.err;

	public PrintStream out(Object stringLike) {
		outStream.print(stringLike);
		return outStream;
	}

	public PrintStream err(Object stringLike) {
		errStream.print(stringLike);
		return errStream;
	}

	public FormatStream format(String template) {
		return new FormatStream(template);
	}

	public class FormatStream {
		String template;
		List<String> args = new ArrayList<>();
		int templateIndex = 0;
		StringBuilder outBuilder = new StringBuilder();

		FormatStream(String template) {
			this.template = template;
		}

		public FormatStream arg(Object stringAlike) {
			var stringArg = Maybe.notNull(stringAlike).match(Object::toString, () -> "null");

			// Find the next parameter location.
			int templateParamIndex = template.indexOf('%', templateIndex);

			// If none exists, throw.
			if(templateParamIndex == -1) {
				var remaining = template.substring(templateIndex);

				var message = String.format(
					"Cannot find a template argument in remaining template string '%s'.",
					remaining);

				throw new NoSuchElementException(message);
			}
			
			outBuilder.append(template.substring(templateIndex, templateParamIndex) + stringArg);
			templateIndex = templateParamIndex + 1;
			return this;
		}

		public PrintStream out() {
			return Console.this.out(outBuilder.toString());
		}
	
		public PrintStream err() {
			return Console.this.err(outBuilder.toString());
		}
	}
}