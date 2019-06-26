package ark.mobile.wiki.jsoup.nodes;

import ark.mobile.wiki.jsoup.parser.Tag;

import java.io.IOException;

/**
 * Represents a {@link TextNode} as an {@link Element}, to enable text nodes to be selected with
 * the {@link ark.mobile.wiki.jsoup.select.Selector} {@code :matchText} syntax.
 */
public class PseudoTextElement extends Element {

    public PseudoTextElement(Tag tag, String baseUri, Attributes attributes) {
        super(tag, baseUri, attributes);
    }

    @Override
    void outerHtmlHead(Appendable accum, int depth, Document.OutputSettings out) throws IOException {
    }

    @Override
    void outerHtmlTail(Appendable accum, int depth, Document.OutputSettings out) throws IOException {
    }
}
