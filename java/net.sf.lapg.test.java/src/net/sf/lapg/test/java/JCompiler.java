package net.sf.lapg.test.java;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.internal.compiler.ClassFile;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.Compiler;
import org.eclipse.jdt.internal.compiler.ICompilerRequestor;
import org.eclipse.jdt.internal.compiler.IErrorHandlingPolicy;
import org.eclipse.jdt.internal.compiler.batch.CompilationUnit;
import org.eclipse.jdt.internal.compiler.batch.FileSystem;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.env.INameEnvironment;
import org.eclipse.jdt.internal.compiler.env.NameEnvironmentAnswer;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.problem.DefaultProblemFactory;

public class JCompiler {

	private final Map<String, CompilationUnit> myUnits;
	private final List<CompilationResult> myResults;
	private final Map<String, byte[]> myClasses;

	public JCompiler() {
		myUnits = new HashMap<String, CompilationUnit>();
		myResults = new ArrayList<CompilationResult>();
		myClasses = new HashMap<String, byte[]>();
	}

	public void addSource(String fqname, String text) {
		myUnits.put(fqname, new CompilationUnit(text.toCharArray(), fqname.replace('.', File.separatorChar) + ".java",
				"UTF-8"));
	}

	public void compile() {
		CompilerOptions options = new CompilerOptions();
		options.sourceLevel = ClassFileConstants.JDK1_5;
		options.targetJDK = ClassFileConstants.JDK1_5;
		options.produceDebugAttributes = ClassFileConstants.ATTR_SOURCE | ClassFileConstants.ATTR_LINES
				| ClassFileConstants.ATTR_VARS;
		String[] classpath = new String[] { "/System/Library/Frameworks/JavaVM.framework/Versions/1.6.0/Classes/classes.jar" };

		Compiler c = new Compiler(getNameEnvironment(classpath), getErrorHandlingPolicy(), options, getCompilerRequestor(),
				new DefaultProblemFactory());

		Collection<CompilationUnit> units = myUnits.values();
		c.compile(units.toArray(new CompilationUnit[units.size()]));
	}

	public ClassLoader getClassLoader(ClassLoader parent) {
		// TODO
		return null;
	}

	public List<CompilationResult> getCompilationResults() {
		return myResults;
	}

	private INameEnvironment getNameEnvironment(String[] classpathNames) {
		return new FileSystem(classpathNames, null, null) {
			@Override
			public NameEnvironmentAnswer findType(char[] typeName, char[][] packageName) {
				String cname = getQualifiedName(packageName) + "." + new String(typeName);
				if (myUnits.containsKey(cname)) {
					return new NameEnvironmentAnswer(myUnits.get(cname), null);
				}
				return super.findType(typeName, packageName);
			}
		};
	}

	private IErrorHandlingPolicy getErrorHandlingPolicy() {
		return new IErrorHandlingPolicy() {

			public boolean proceedOnErrors() {
				return true;
			}

			public boolean stopOnFirstError() {
				return false;
			}

		};
	}

	private static String getQualifiedName(char[][] name) {
		StringBuilder sb = new StringBuilder();
		for (char[] cc : name) {
			if (sb.length() != 0) {
				sb.append('.');
			}
			sb.append(new String(cc));
		}
		return sb.toString();
	}

	private ICompilerRequestor getCompilerRequestor() {
		return new ICompilerRequestor() {

			public void acceptResult(CompilationResult result) {
				for (ClassFile file : result.getClassFiles()) {
					myClasses.put(getQualifiedName(file.getCompoundName()), file.getBytes());
				}
				myResults.add(result);
			}
		};
	}
}
