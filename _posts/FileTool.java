import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;

public class FileTool {

	private static HashSet<String> dir = new LinkedHashSet<String>();

	public static void main(String[] args) {
		traverse(new File("/Users/lijun/work/projects/android/android"));
		Iterator<String> iterator = dir.iterator();
		while (iterator.hasNext()) {
			String ling = iterator.next();
			if (!(ling.contains("/tests/") || ling.contains("/cts/")
					|| ling.contains("/test/") || ling.contains("/testapps/")
					|| ling.contains("/javatests/")
					|| ling.contains("/samples/")
					|| ling.contains("/prebuilts/")
					|| ling.contains("/eclipse/"))) {
				System.out.println("<root type=\"simple\" url=\"file://"
						+ ling + "\" />");
			}

		}
	}

	public static String traverse(File file) {
		if (file.isDirectory()) {
			File[] sub = file.listFiles();
			if (sub != null && sub.length > 0) {
				for (File f : sub) {
					String a = traverse(f);
					// if (a > 0) {
					// if (a == 1 && !f.getPath().contains("/tests/")
					// && !f.getPath().contains("/cts/")
					// && !f.getPath().contains("/test/")
					// && !f.getPath().contains("/testapps")
					// && !f.getPath().contains("/javatests")) {
					//
					// System.out.println("<root type=\"simple\" url=\"file://"
					// + f.getPath() + "\" />");
					// }
					// return a - 1;
				}
			}
			// }
			return "";
		} else if (file.getName().endsWith(".java")) {
			FileReader fileReader = null;
			BufferedReader bufferedReader = null;
			if (file.getPath().contains("/Library/a5/android/libcore/dalvik/")) {
				file.hashCode();
			}
			try {
				File origin = file;
				fileReader = new FileReader(file.getPath());
				bufferedReader = new BufferedReader(fileReader);
				for (int i = 0; i < 50; ++i) {
					String line = bufferedReader.readLine();
					if (line != null && line.startsWith("package")) {
						String[] pn = line.split(" ");
						String pname = pn[1];
						String[] layers = pname.split("\\.");
						for (int j = 0; j <= layers.length; ++j) {
							file = file.getParentFile();
						}
						if ("/Library/a5/android".equals(file.getPath())) {
							file.hashCode();
						}
						dir.add(file.getPath());
						return file.getPath();
					}
				}
				return "";
			} catch (Exception e) {
				e.printStackTrace();
				return "";
			} finally {
				if (fileReader != null) {
					try {
						fileReader.close();
					} catch (Exception e2) {
						e2.printStackTrace();
					}

				}
			}
		} else {
			return "";
		}
	}
}
