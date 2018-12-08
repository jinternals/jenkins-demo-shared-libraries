import org.assertj.core.util.Files
import org.junit.*
import com.lesfurets.jenkins.unit.*
import java.nio.charset.Charset
import static com.lesfurets.jenkins.unit.MethodSignature.method
import static org.assertj.core.api.Assertions.assertThat

class GenerateVersion extends BasePipelineTest {

    def generateVersion

    @Before
    void setUp() {
        super.setUp()
        generateVersion = loadScript("vars/generateVersion.groovy")
    }

    @Test
    void testGenerateVersion() throws Exception {
        // given
        helper.registerAllowedMethod(method("readFile", String.class), { file ->
            return Files.contentOf(new File(file), Charset.forName("UTF-8"))
        })
        // when
        def pomVersion = generateVersion(pom: 'src/test/resources/test-pom.xml')

        // then
        assertThat(pomVersion)
                .isEqualTo("1.0")
    }
}
