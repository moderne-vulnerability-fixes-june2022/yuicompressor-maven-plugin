package net_alchim31_maven_yuicompressor;

import com.google.common.collect.Lists;
import junit.framework.TestCase;
import org.codehaus.plexus.util.FileUtils;
import org.sonatype.plexus.build.incremental.DefaultBuildContext;

import java.io.File;
import java.nio.file.Files;
import java.util.Collection;
import java.util.HashSet;

public class AggregationTestCase extends TestCase {
    private File dir_;

    private DefaultBuildContext defaultBuildContext = new DefaultBuildContext();

    @Override
    protected void setUp() throws Exception {
        dir_ = Files.createTempDirectory(this.getClass().getName() + "-test").toFile();
    }

    @Override
    protected void tearDown() throws Exception {
        FileUtils.deleteDirectory(dir_);
    }

    public void test0to1() throws Exception {
        Aggregation target = new Aggregation();
        target.output = new File(dir_, "output.js");

        assertFalse(target.output.exists());
        target.run(null, defaultBuildContext);
        assertFalse(target.output.exists());

        target.includes = new String[]{};
        assertFalse(target.output.exists());
        target.run(null, defaultBuildContext);
        assertFalse(target.output.exists());

        target.includes = new String[]{"**/*.js"};
        assertFalse(target.output.exists());
        target.run(null, defaultBuildContext);
        assertFalse(target.output.exists());
    }


    public void test1to1() throws Exception {
        File f1 = new File(dir_, "01.js");
        FileUtils.fileWrite(f1.getAbsolutePath(), "1");
        Aggregation target = new Aggregation();
        target.output = new File(dir_, "output.js");
        target.includes = new String[]{f1.getName()};

        assertFalse(target.output.exists());
        target.run(null, defaultBuildContext);
        assertTrue(target.output.exists());
        assertEquals(FileUtils.fileRead(f1), FileUtils.fileRead(target.output));
    }

    public void test2to1() throws Exception {
        File f1 = new File(dir_, "01.js");
        FileUtils.fileWrite(f1.getAbsolutePath(), "1");

        File f2 = new File(dir_, "02.js");
        FileUtils.fileWrite(f2.getAbsolutePath(), "22\n22");

        Aggregation target = new Aggregation();
        target.output = new File(dir_, "output.js");

        target.includes = new String[]{f1.getName(), f2.getName()};
        assertFalse(target.output.exists());
        target.run(null, defaultBuildContext);
        assertTrue(target.output.exists());
        assertEquals(FileUtils.fileRead(f1) + FileUtils.fileRead(f2), FileUtils.fileRead(target.output));

        target.output.delete();
        target.includes = new String[]{"*.js"};
        assertFalse(target.output.exists());
        target.run(null, defaultBuildContext);
        assertTrue(target.output.exists());
        assertEquals(FileUtils.fileRead(f1) + FileUtils.fileRead(f2), FileUtils.fileRead(target.output));
    }

    public void testNoDuplicateAggregation() throws Exception {
        File f1 = new File(dir_, "01.js");
        FileUtils.fileWrite(f1.getAbsolutePath(), "1");

        File f2 = new File(dir_, "02.js");
        FileUtils.fileWrite(f2.getAbsolutePath(), "22\n22");

        Aggregation target = new Aggregation();
        target.output = new File(dir_, "output.js");

        target.includes = new String[]{f1.getName(), f1.getName(), f2.getName()};
        assertFalse(target.output.exists());
        target.run(null, defaultBuildContext);
        assertTrue(target.output.exists());
        assertEquals(FileUtils.fileRead(f1) + FileUtils.fileRead(f2), FileUtils.fileRead(target.output));

        target.output.delete();
        target.includes = new String[]{f1.getName(), "*.js"};
        assertFalse(target.output.exists());
        target.run(null, defaultBuildContext);
        assertTrue(target.output.exists());
        assertEquals(FileUtils.fileRead(f1) + FileUtils.fileRead(f2), FileUtils.fileRead(target.output));
    }

    public void test2to1Order() throws Exception {
        File f1 = new File(dir_, "01.js");
        FileUtils.fileWrite(f1.getAbsolutePath(), "1");

        File f2 = new File(dir_, "02.js");
        FileUtils.fileWrite(f2.getAbsolutePath(), "2");

        Aggregation target = new Aggregation();
        target.output = new File(dir_, "output.js");

        target.includes = new String[]{f2.getName(), f1.getName()};
        assertFalse(target.output.exists());
        target.run(null, defaultBuildContext);
        assertTrue(target.output.exists());
        assertEquals(FileUtils.fileRead(f2) + FileUtils.fileRead(f1), FileUtils.fileRead(target.output));
    }

    public void test2to1WithNewLine() throws Exception {
        File f1 = new File(dir_, "01.js");
        FileUtils.fileWrite(f1.getAbsolutePath(), "1");

        File f2 = new File(dir_, "02.js");
        FileUtils.fileWrite(f2.getAbsolutePath(), "22\n22");

        Aggregation target = new Aggregation();
        target.output = new File(dir_, "output.js");
        target.insertNewLine = true;
        target.includes = new String[]{f1.getName(), f2.getName()};

        assertFalse(target.output.exists());
        target.run(null, defaultBuildContext);
        assertTrue(target.output.exists());
        assertEquals(FileUtils.fileRead(f1) + "\n" + FileUtils.fileRead(f2) + "\n", FileUtils.fileRead(target.output));
    }

    public void testAbsolutePathFromInside() throws Exception {
        File f1 = new File(dir_, "01.js");
        FileUtils.fileWrite(f1.getAbsolutePath(), "1");

        File f2 = new File(dir_, "02.js");
        FileUtils.fileWrite(f2.getAbsolutePath(), "22\n22");

        Aggregation target = new Aggregation();
        target.output = new File(dir_, "output.js");

        target.includes = new String[]{f1.getAbsolutePath(), f2.getName()};
        assertFalse(target.output.exists());
        target.run(null, defaultBuildContext);
        assertTrue(target.output.exists());
        assertEquals(FileUtils.fileRead(f1) + FileUtils.fileRead(f2), FileUtils.fileRead(target.output));
    }

    public void testAbsolutePathFromOutside() throws Exception {
        File f1 = File.createTempFile("test-01", ".js");
        try {
            FileUtils.fileWrite(f1.getAbsolutePath(), "1");

            File f2 = new File(dir_, "02.js");
            FileUtils.fileWrite(f2.getAbsolutePath(), "22\n22");

            Aggregation target = new Aggregation();
            target.output = new File(dir_, "output.js");

            target.includes = new String[]{f1.getAbsolutePath(), f2.getName()};
            assertFalse(target.output.exists());
            target.run(null, defaultBuildContext);
            assertTrue(target.output.exists());
            assertEquals(FileUtils.fileRead(f1) + FileUtils.fileRead(f2), FileUtils.fileRead(target.output));
        } finally {
            f1.delete();
        }
    }

    public void testAutoExcludeWildcards() throws Exception {
        File f1 = new File(dir_, "01.js");
        FileUtils.fileWrite(f1.getAbsolutePath(), "1");

        File f2 = new File(dir_, "02.js");
        FileUtils.fileWrite(f2.getAbsolutePath(), "22\n22");

        Aggregation target = new Aggregation();
        target.autoExcludeWildcards = true;
        target.output = new File(dir_, "output.js");

        Collection<File> previouslyIncluded = new HashSet<File>();
        previouslyIncluded.add(f1);

        target.includes = new String[]{f1.getName(), f2.getName()};
        assertFalse(target.output.exists());
        target.run(previouslyIncluded, defaultBuildContext);
        assertTrue(target.output.exists());
        assertEquals(FileUtils.fileRead(f1) + FileUtils.fileRead(f2), FileUtils.fileRead(target.output));

        target.output.delete();
        target.includes = new String[]{"*.js"};
        assertFalse(target.output.exists());
        //f1 was in previouslyIncluded so it is not included
        assertEquals(target.run(previouslyIncluded, defaultBuildContext), Lists.newArrayList(f2));
        assertTrue(target.output.exists());
        assertEquals(FileUtils.fileRead(f2), FileUtils.fileRead(target.output));
    }
}
