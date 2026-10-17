package id.maetaruna.assessment.userapi.config;

import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.core.util.DefaultIndenter;
import tools.jackson.core.util.DefaultPrettyPrinter;
import tools.jackson.core.util.Separators;
import tools.jackson.databind.SerializationFeature;

@Configuration
public class JsonFormatConfig {

    // Matches the layout of the sample in the assignment: two-space indent, `"key": value`, and
    // every array element on its own line. Jackson's default printer writes `"key" : value`.
    @Bean
    JsonMapperBuilderCustomizer assignmentJsonLayout() {
        return builder -> {
            DefaultIndenter indenter = new DefaultIndenter("  ", "\n");
            DefaultPrettyPrinter printer = new DefaultPrettyPrinter(
                    Separators.createDefaultInstance().withObjectNameValueSpacing(Separators.Spacing.AFTER));
            printer.indentObjectsWith(indenter);
            printer.indentArraysWith(indenter);
            builder.enable(SerializationFeature.INDENT_OUTPUT).defaultPrettyPrinter(printer);
        };
    }
}
