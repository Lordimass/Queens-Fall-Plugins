package net.lordimass.dialogue.parameter.eventTag;

import net.lordimass.dialogue.parameter.ParameterContext;
import net.lordimass.dialogue.parameter.ParameterProcessor;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents an HTML-like tag that can be placed inline in dialogue strings to run custom effects
 * when it's encountered by the TypewriterEffect parsing.
 * <br>
 * Currently, parameters to these tags must be provided in the order they are given in the
 * constructor.
 */
public final class EventTagProcessor<C> extends ParameterProcessor<C> {
    private final Class<C> contextType;
    private final EventTagResolver<C> resolver;
    public final String[] parameters;
    public final Pattern pattern;

    public EventTagProcessor(String key, Class<C> contextType, EventTagResolver<C> resolver, String[] parameters) {
        super(key, contextType, resolver);
        this.contextType = contextType;
        this.resolver = resolver;
        this.parameters = parameters;
        this.pattern = constructRegex();
    }

    @Override
    public boolean supports(ParameterContext ctx) {
        EventTagParameterContext eCtx = EventTagParameterContext.parameterContextAs(ctx);
        Matcher matcher = this.pattern.matcher(eCtx.token);
        return ctx.has(contextType) && matcher.matches();
    }

    public boolean matches(String token) {
        Matcher matcher = this.pattern.matcher(token);
        return matcher.matches();
    }

    @Override
    public String resolve(ParameterContext ctx) {
        EventTagParameterContext eCtx = EventTagParameterContext.parameterContextAs(ctx);
        Map<String, String> params = new HashMap<>();
        if (eCtx.token == null) return null;
        Matcher matcher  = this.pattern.matcher(eCtx.token);
        if (!matcher.matches() || !ctx.has(contextType)) return null;
        for (String param : matcher.namedGroups().keySet()) {
            params.put(param, matcher.group(param));
        }
        boolean result = resolver.resolve(ctx.get(contextType), params);
        return result ? "" : null;
    }

    private Pattern constructRegex() {
        StringBuilder regexSb = new StringBuilder("<"+ key);
        for (String parameter : parameters) {
            regexSb
                .append(" ")
                .append(parameter)
                .append("=\"(?<")
                .append(parameter)
                .append(">.*?)\"");
        }
        regexSb.append(">");
        return Pattern.compile(regexSb.toString());
    }
}
