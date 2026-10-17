package id.maetaruna.assessment.postapi.dto;

import java.util.List;
import tools.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import tools.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import tools.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

@JacksonXmlRootElement(localName = "root")
public record PostsXml(
        @JacksonXmlElementWrapper(useWrapping = false)
        @JacksonXmlProperty(localName = "row")
        List<PostRow> rows) {
}
