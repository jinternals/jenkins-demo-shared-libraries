import org.junit.*
import com.lesfurets.jenkins.unit.*

class BaseTest extends BasePipelineTest {
    @Before
    void setUp() {
        super.setUp()

        // load all steps from vars directory
        new File("vars").eachFile { file ->
            def name = file.name.replace(".groovy", "")

            // register step with no args, example: toAlphanumeric()
            helper.registerAllowedMethod(name, []) { ->
                loadScript(file.path)()
            }

            // register step with Map arg, example: toAlphanumeric(text: "a")
            helper.registerAllowedMethod(name, [ Map ]) { opts ->
                loadScript(file.path)(opts)
            }
        }
    }
}