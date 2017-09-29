/*
  Copyright 2017 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/

package ec.app.ecsuite;

import ec.EvolutionState;
import ec.Evolve;
import ec.util.Parameter;
import ec.util.ParameterDatabase;
import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

/**
 * System tests that run every example parameter file for a couple generations
 * and ensure that they don't crash.
 * 
 * @author Eric O. Scott
 */
@RunWith(Parameterized.class)
public class SuiteExamplesTest {
    
    /** List the subdirectories of a directory. */
    private static List<File> getSubdirectories(final File dir) {
        assert(dir != null);
        assert(dir.isDirectory());
        final File[] subdirs = dir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.isDirectory();
            }
        });
        final List<File> result = new ArrayList();
        result.addAll(Arrays.asList(subdirs));
        return result;
    }
    
    /** List the parameter files in a directory. */
    private static List<Object[]> getParamFiles(final File dir, final List<String> exclude) {
        assert(dir != null);
        final File[] appParams = dir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".params");
            }
        });
        final List<Object[]> result = new ArrayList();
        for (File f : appParams)
            if (!exclude.contains(f.getAbsolutePath()))
            result.add(new Object[] { f.getPath() });
        return result;
    }
    
    @Parameterized.Parameters(name = "{index}: {0}")
    /** Loads all parameter files in the ec/app subdirectories for testing, except for those in the hard-coded exclude list. */
    public static Collection<Object[]> data() {
        final ArrayList<Object[]> paramFiles = new ArrayList();
        
        final List<String> exclude = Arrays.asList(new String[] {
            // Parent files; can't be run directly
            new File("src/main/java/ec/app/moosuite/moosuite.params").getAbsolutePath(),
            new File("src/main/java/ec/app/moosuite/nsga2.params").getAbsolutePath(),
            new File("src/main/java/ec/app/moosuite/spea2.params").getAbsolutePath(),
            
            // Distributed examples; need their own test runner.
            new File("src/main/java/ec/app/star/ant.master.params").getAbsolutePath(),
            new File("src/main/java/ec/app/star/ant.slave.params").getAbsolutePath(),
            new File("src/main/java/ec/app/star/coevolve1.master.params").getAbsolutePath(),
            new File("src/main/java/ec/app/star/coevolve1.slave.params").getAbsolutePath(),
            new File("src/main/java/ec/app/star/coevolve2.master.params").getAbsolutePath(),
            new File("src/main/java/ec/app/star/coevolve2.slave.params").getAbsolutePath(),
            new File("src/main/java/ec/app/star/mastermeta.params").getAbsolutePath(),
            new File("src/main/java/ec/app/star/slavemeta.params").getAbsolutePath(),
            
             // XXX A broken test; needs to be fixed and removed from exclude list.
            new File("src/main/java/ec/app/moosuite/kur-spea2.params").getAbsolutePath()
        });
        
        // Test all the parameter files inside each app directory
        final File appsRoot = new File("src/main/java/ec/app/");
        final List<File> appDirs = getSubdirectories(appsRoot);
        for (File d : appDirs) {
            paramFiles.addAll(getParamFiles(d, exclude));
        }
        return paramFiles;
    }
    
    @Parameterized.Parameter
    public String examplePath;

    @Test
    public void testExample() {
        try {
            Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Testing " + examplePath);
            final ParameterDatabase parameters = new ParameterDatabase(new File(examplePath));
            parameters.set(new Parameter(EvolutionState.P_GENERATIONS), "2");
            parameters.set(new Parameter(Evolve.P_SILENT), "true");
            // Can't use Evolve.main() because it calls System.exit()
            final EvolutionState state = Evolve.initialize(parameters, 0);
            state.run(EvolutionState.C_STARTED_FRESH);
            // No exception is success.
        }
        catch (IOException e) {
            fail(e.toString());
        }
    }
}
