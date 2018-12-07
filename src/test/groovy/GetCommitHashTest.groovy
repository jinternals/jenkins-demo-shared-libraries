import org.junit.*
import com.lesfurets.jenkins.unit.*
import static groovy.test.GroovyAssert.*

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

        // create mock sh step
        helper.registerAllowedMethod("sh", [ String ]) { hash }

        // call getCommitHash and check result
        def result = getCommitHash()
        assertEquals "result:", hash, result
    }

}