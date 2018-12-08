import org.junit.*
import com.lesfurets.jenkins.unit.*
import static groovy.test.GroovyAssert.*
import static com.lesfurets.jenkins.unit.MethodSignature.method
import org.assertj.core.util.Files
import java.nio.charset.Charset
import static org.assertj.core.api.Assertions.assertThat

class GetCommitHashTest extends BasePipelineTest {

    def getCommitHash

    @Before
    void setUp() {
        super.setUp()
        // load getCommitHash
        getCommitHash = loadScript("vars/getCommitHash.groovy")
    }

    @Test
    void testCall() {
        def hash = "9ee0fbdd081d0fa9e9d40dd904309be391e0fb2b"

        helper.registerAllowedMethod("sh", [ String ]) { hash }

        def result = getCommitHash()

        assertThat(hash)
                .isEqualTo(result)
    }

}