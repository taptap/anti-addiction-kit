package org.springframework.data.repository.query.parser;

import com.taptap.tds.registration.server.core.persistence.EntityInformation;
import org.springframework.data.repository.query.parser.Part.Type;
import org.springframework.data.repository.query.parser.PartTree.OrPart;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class UpdatePartTree implements Iterable<OrPart> {

    /*
     * We look for a pattern of: keyword followed by
     *
     *  an upper-case letter that has a lower-case variant \p{Lu}
     * OR
     *  any other letter NOT in the BASIC_LATIN Uni-code Block \\P{InBASIC_LATIN} (like Chinese, Korean, Japanese, etc.).
     *
     * @see <a href="http://www.regular-expressions.info/unicode.html">http://www.regular-expressions.info/unicode.html</a>
     * @see <a href="http://docs.oracle.com/javase/7/docs/api/java/util/regex/Pattern.html#ubc">Pattern</a>
     */
    private static final String KEYWORD_TEMPLATE = "(%s)(?=(\\p{Lu}|\\P{InBASIC_LATIN}))";

    private static final Pattern PREFIX_TEMPLATE = Pattern.compile("^(update)(\\p{Lu}.*?)??By");

    /**
     * The projections, for example "updateAgeAndGenderByName" would have the projections "age, gender".
     */
    private final List<String> projections;

    /**
     * The subject, for example "updateUserByName" would have the predicate "Name".
     */
    private final Predicate predicate;

    public UpdatePartTree(String source, EntityInformation entityInformation) {

        Assert.notNull(source, "Source must not be null");
        Assert.notNull(entityInformation, "Entity information must not be null");

        String finalSource = source;
        Matcher matcher = PREFIX_TEMPLATE.matcher(finalSource);
        if (!matcher.find()) {
            finalSource = appendByPrimaryKeysExpression(finalSource, entityInformation);
        }

        matcher = PREFIX_TEMPLATE.matcher(finalSource);
        if (!matcher.find()) {
            throw new IllegalArgumentException("Illegal source [" + source + "] of UpdatePartTree");
        }

        String subject = matcher.group(2);
        if (!StringUtils.hasText(subject)) {
            throw new IllegalArgumentException("Illegal source [" + source + "] of UpdatePartTree");
        }

        this.projections = Stream.of(split(subject, "And"))
                .collect(Collectors.mapping(StringUtils::uncapitalize, Collectors.toList()));
        this.predicate = new Predicate(finalSource.substring(matcher.group().length()), entityInformation.getEntityClass());
    }

    private static String appendByPrimaryKeysExpression(String source, EntityInformation entityInformation) {
        StringBuilder builder = new StringBuilder(source);
        builder.append("By");
        boolean first = true;
        for (String identityProperty : entityInformation.getIdProperties()) {
            if (first) {
                first = false;
            } else {
                builder.append("And");
            }
            builder.append(StringUtils.capitalize(identityProperty));
        }
        return builder.toString();
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Iterable#iterator()
     */
    @Override
    public Iterator<OrPart> iterator() {
        return predicate.iterator();
    }

    public List<String> getProjections() {
        return projections;
    }

    /**
     * Returns an {@link Iterable} of all parts contained in the {@link PartTree}.
     * 
     * @return the iterable {@link Part}s
     */
    public Iterable<Part> getParts() {

        List<Part> result = new ArrayList<>();
        for (OrPart orPart : this) {
            for (Part part : orPart) {
                result.add(part);
            }
        }
        return result;
    }

    /**
     * Returns all {@link Part}s of the {@link PartTree} of the given {@link Type}.
     * 
     * @param type
     * @return
     */
    public Iterable<Part> getParts(Type type) {

        List<Part> result = new ArrayList<>();

        for (Part part : getParts()) {
            if (part.getType().equals(type)) {
                result.add(part);
            }
        }

        return result;
    }

    @Override
    public String toString() {
        return StringUtils.collectionToDelimitedString(predicate.nodes, " or ");
    }

    /**
     * Splits the given text at the given keywords. Expects camel-case style to only match concrete keywords and not
     * derivatives of it.
     * 
     * @param text the text to split
     * @param keyword the keyword to split around
     * @return an array of split items
     */
    private static String[] split(String text, String keyword) {
        Pattern pattern = Pattern.compile(String.format(KEYWORD_TEMPLATE, keyword));
        return pattern.split(text);
    }

    /**
     * Represents the predicate part of the query.
     * 
     * @author Oliver Gierke
     * @author Phil Webb
     */
    private static class Predicate {

        private static final Pattern ALL_IGNORE_CASE = Pattern.compile("AllIgnor(ing|e)Case");

        private final List<OrPart> nodes = new ArrayList<>();

        private boolean alwaysIgnoreCase;

        public Predicate(String predicate, Class<?> domainClass) {
            buildTree(detectAndSetAllIgnoreCase(predicate), domainClass);
        }

        private String detectAndSetAllIgnoreCase(String predicate) {

            Matcher matcher = ALL_IGNORE_CASE.matcher(predicate);

            if (matcher.find()) {
                alwaysIgnoreCase = true;
                predicate = predicate.substring(0, matcher.start()) + predicate.substring(matcher.end(), predicate.length());
            }

            return predicate;
        }

        private void buildTree(String source, Class<?> domainClass) {

            String[] split = split(source, "Or");
            for (String part : split) {
                nodes.add(new OrPart(part, domainClass, alwaysIgnoreCase));
            }
        }

        public Iterator<OrPart> iterator() {
            return nodes.iterator();
        }
    }
}
