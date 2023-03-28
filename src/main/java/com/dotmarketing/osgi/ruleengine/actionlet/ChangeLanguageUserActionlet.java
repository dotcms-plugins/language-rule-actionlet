package com.dotmarketing.osgi.ruleengine.actionlet;

import com.dotcms.repackage.com.google.common.base.Preconditions;
import com.dotmarketing.business.APILocator;
import com.dotmarketing.business.web.WebAPILocator;
import com.dotmarketing.portlets.languagesmanager.model.Language;
import com.dotmarketing.portlets.rules.RuleComponentInstance;
import com.dotmarketing.portlets.rules.actionlet.RuleActionlet;
import com.dotmarketing.portlets.rules.model.ParameterModel;
import com.dotmarketing.portlets.rules.parameter.ParameterDefinition;
import com.dotmarketing.portlets.rules.parameter.display.TextInput;
import com.dotmarketing.portlets.rules.parameter.type.TextType;
import com.dotmarketing.util.Logger;
import com.dotmarketing.util.WebKeys;
import com.liferay.portal.language.LanguageUtil;
import org.apache.commons.lang.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Locale;
import java.util.Map;

import static com.dotcms.repackage.com.google.common.base.Preconditions.checkState;

/**
 * This actionlet will change the user session language
 *
 * @author jsanca
 * @version 1.0
 * @since 10/17/2016
 */
public class ChangeLanguageUserActionlet extends RuleActionlet<ChangeLanguageUserActionlet.Instance> {

    /**
     * Actionlet Parameters Id
     */
    public static final String INPUT_LANGUAGE_KEY = "language";
    private static final String I18N_BASE = "api.system.ruleengine.actionlet.ChangeLanguageUser";

    public ChangeLanguageUserActionlet() {
        super(I18N_BASE,
                new ParameterDefinition<>(1, INPUT_LANGUAGE_KEY, new TextInput<>(new TextType().minLength(1))));
    }

    /**
     * Create a instance of the VelocityScriptingActionlet with the provided parameters
     */
    @Override
    public Instance instanceFrom(Map<String, ParameterModel> parameters) {
        return new Instance(parameters);
    }

    /**
     * This method sets the language in the session and the locale in the request
     *
     * @param request  Http servlet request
     * @param response Http servlet response
     * @param instance Instance of the actionlet
     * @return true if the code could be evaluated, false if not
     */
    @Override
    public boolean evaluate(final HttpServletRequest request,
                            final HttpServletResponse response,
                            final Instance instance) {
        boolean success = false;
        try {

            final long languageId = LanguageUtil.getLanguageId(instance.language);
            if (-1 != languageId) {

                final Language language = APILocator.getLanguageAPI().getLanguage(languageId);
                final HttpSession sessionOpt = request.getSession();
                if (null != sessionOpt) {

                    Logger.info(this, "Changing language to: " + language);
                    request.setAttribute(WebKeys.HTMLPAGE_LANGUAGE + ".current", String.valueOf(language.getId()));
                    sessionOpt.setAttribute(WebKeys.HTMLPAGE_LANGUAGE, String.valueOf(language.getId()));
                    WebAPILocator.getLanguageWebAPI().getLanguage(request);
                    success = true;
                }
            }
        } catch (Exception e) {
            Logger.error(ChangeLanguageUserActionlet.class,
                    "Error executing Changing Language Actionlet: " + e.getMessage(), e);
        }
        return success;
    }


    public class Instance implements RuleComponentInstance {

        private final String language;

        public Instance(final Map<String, ParameterModel> parameters) {

            checkState(parameters != null && parameters.size() == 1,
                    "Change Language Condition Type requires parameter '%s'.", INPUT_LANGUAGE_KEY);
            assert parameters != null;
            this.language = parameters.get(INPUT_LANGUAGE_KEY).getValue();
            Preconditions.checkArgument(StringUtils.isNotBlank(this.language),
                    "ChangeLanguageUserAction requires valid language.");

        }
    }
}
