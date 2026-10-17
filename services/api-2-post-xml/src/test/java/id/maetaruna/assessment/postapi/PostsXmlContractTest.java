package id.maetaruna.assessment.postapi;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import javax.xml.transform.Source;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.w3c.dom.Node;
import org.xmlunit.builder.Input;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.diff.ComparisonResult;
import org.xmlunit.diff.ComparisonType;
import org.xmlunit.diff.DefaultNodeMatcher;
import org.xmlunit.diff.DifferenceEvaluators;
import org.xmlunit.diff.Diff;
import org.xmlunit.diff.ElementSelectors;
import org.xmlunit.xpath.JAXPXPathEngine;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(PostgresContainerConfig.class)
class PostsXmlContractTest {

    @LocalServerPort
    int port;

    @Test
    void firstRowsMatchTheSampleFromTheAssignment() throws IOException, InterruptedException {
        HttpResponse<String> response;
        try (HttpClient client = HttpClient.newHttpClient()) {
            response = client.send(
                    HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api/posts")).build(),
                    HttpResponse.BodyHandlers.ofString());
        }
        assertThat(response.statusCode()).isEqualTo(200);
        String actual = response.body();
        String sample = new ClassPathResource("contract/api-2-sample.xml").getContentAsString(StandardCharsets.UTF_8);

        assertThat(actual).containsPattern("^<\\?xml version=['\"]1\\.0['\"] encoding=['\"]UTF-8['\"]\\?>");
        assertThat(new JAXPXPathEngine().evaluate("count(/root/row)", source(actual))).isEqualTo("100");

        // The sample in the assignment truncates each body and prints newlines as a literal "\n",
        // so bodies are compared by prefix below and excluded from the exact structural diff.
        Diff diff = DiffBuilder.compare(sample)
                .withTest(actual)
                .ignoreWhitespace()
                .checkForSimilar()
                .withNodeFilter(PostsXmlContractTest::isWithinFirstTwoRows)
                .withNodeMatcher(new DefaultNodeMatcher(ElementSelectors.byName))
                .withDifferenceEvaluator(DifferenceEvaluators.chain(
                        DifferenceEvaluators.Default,
                        (comparison, outcome) -> comparison.getType() == ComparisonType.TEXT_VALUE
                                && "body".equals(comparison.getControlDetails().getTarget().getParentNode().getNodeName())
                                ? ComparisonResult.SIMILAR
                                : outcome))
                .build();
        assertThat(diff.hasDifferences()).as(diff.toString()).isFalse();

        for (int row = 1; row <= 2; row++) {
            String expectedPrefix = normalize(xpath(sample, "/root/row[" + row + "]/body"));
            String actualBody = normalize(xpath(actual, "/root/row[" + row + "]/body"));
            assertThat(actualBody).startsWith(expectedPrefix);
        }
    }

    private static boolean isWithinFirstTwoRows(Node node) {
        if (!"row".equals(node.getNodeName())) {
            return true;
        }
        int position = 1;
        for (Node sibling = node.getPreviousSibling(); sibling != null; sibling = sibling.getPreviousSibling()) {
            if ("row".equals(sibling.getNodeName())) {
                position++;
            }
        }
        return position <= 2;
    }

    private static String xpath(String xml, String expression) {
        return new JAXPXPathEngine().evaluate(expression, source(xml));
    }

    private static Source source(String xml) {
        return Input.fromString(xml).build();
    }

    private static String normalize(String text) {
        return text.replace("\\n", " ").replaceAll("\\s+", " ").trim();
    }
}
