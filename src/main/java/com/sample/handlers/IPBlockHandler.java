package com.sample.handlers;

import java.util.Map;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axis2.AxisFault;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpStatus;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.rest.AbstractHandler;
import org.apache.synapse.transport.passthru.PassThroughConstants;
import org.apache.synapse.transport.passthru.util.RelayUtils;
import org.wso2.carbon.apimgt.gateway.APIMgtGatewayConstants;
import org.wso2.carbon.apimgt.gateway.handlers.Utils;
import org.wso2.carbon.apimgt.gateway.handlers.throttling.APIThrottleConstants;

/**
 * Handler implementation to block requests based on Client IP addresses
 * 
 * This is a sample handler implementation which checks the extracted Client IP
 * address against the specified IP Regex pattern in the Handler
 */
public class IPBlockHandler extends AbstractHandler {

    private String ipRegexPattern;

    private static final String HEADER_X_FORWARDED_FOR = "X-FORWARDED-FOR";
    private static final Log log = LogFactory.getLog(IPBlockHandler.class);

    @Override
    public boolean handleRequest(MessageContext messageContext) {
        String clientIP = getClientIP(messageContext);

        if (log.isDebugEnabled()) {
            log.debug("Handling request and validating the Client IP : " + clientIP
                    + " against the specified condition : " + ipRegexPattern);
        }

        if (StringUtils.isNotEmpty(ipRegexPattern) && clientIP.matches(ipRegexPattern)) {
            handleBlockedRequest(messageContext);
            return false;
        }
        return true;
    }

    @Override
    public boolean handleResponse(MessageContext messageContext) {
        return true;
    }

    /**
     * Extract client ip from the message context and transport headers
     * 
     * @param context message context
     * @return client ip
     */
    @SuppressWarnings("unchecked")
    private String getClientIP(MessageContext context) {
        if (log.isDebugEnabled()) {
            log.debug("Extracting Client IP from Headers");
        }
        String clientIP;

        org.apache.axis2.context.MessageContext axis2Context = ((Axis2MessageContext) context).getAxis2MessageContext();
        Map<String, Object> headers = (Map<String, Object>) axis2Context
                .getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);
        String xForwardedForHeader = (String) headers.get(HEADER_X_FORWARDED_FOR);
        if (!StringUtils.isEmpty(xForwardedForHeader)) {
            clientIP = xForwardedForHeader;
            int index = xForwardedForHeader.indexOf(',');
            if (index > -1) {
                clientIP = clientIP.substring(0, index);
            }
        } else {
            clientIP = (String) axis2Context.getProperty(org.apache.axis2.context.MessageContext.REMOTE_ADDR);
        }

        if (log.isDebugEnabled()) {
            log.debug("Extracted Client IP of the request : " + clientIP);
        }

        return clientIP;
    }

    /**
     * method to handle blocked requests
     *
     * @param context message context
     */
    private void handleBlockedRequest(MessageContext context) {
        String message = "Request blocked";
        String description = "You have been blocked from accessing the resource";
        int statusCode = HttpStatus.SC_FORBIDDEN;

        context.setProperty(SynapseConstants.ERROR_CODE, "-1");
        context.setProperty(SynapseConstants.ERROR_MESSAGE, message);
        context.setProperty(SynapseConstants.ERROR_DETAIL, description);
        context.setProperty(APIMgtGatewayConstants.HTTP_RESPONSE_STATUS_CODE, statusCode);

        org.apache.axis2.context.MessageContext axis2MC = ((Axis2MessageContext) context).getAxis2MessageContext();
        axis2MC.setProperty(PassThroughConstants.MESSAGE_BUILDER_INVOKED, Boolean.TRUE);
        try {
            RelayUtils.consumeAndDiscardMessage(axis2MC);
        } catch (AxisFault axisFault) {
            log.error("Error occurred while consuming and discarding the message", axisFault);
        }

        if (context.isDoingPOX() || context.isDoingGET()) {
            Utils.setFaultPayload(context, getFaultPayload(message, description));
        } else {
            Utils.setSOAPFault(context, "Server", message, description);
        }

        Utils.sendFault(context, statusCode);
    }

    /**
     * method to construct a fault payload to respond to the client with the
     * specified error message and description
     * 
     * @param message     error message
     * @param description error description
     * @return fault payload
     */
    private OMElement getFaultPayload(String message, String description) {
        OMFactory factory = OMAbstractFactory.getOMFactory();
        OMNamespace namespace = factory.createOMNamespace(APIThrottleConstants.API_THROTTLE_NS,
                APIThrottleConstants.API_THROTTLE_NS_PREFIX);
        OMElement payload = factory.createOMElement("fault", namespace);
        OMElement errorMessage = factory.createOMElement("message", namespace);
        errorMessage.setText(message);
        OMElement errorDescription = factory.createOMElement("description", namespace);
        errorDescription.setText(description);

        payload.addChild(errorMessage);
        payload.addChild(errorDescription);
        return payload;
    }

    public void setIPRegex(String regex) {
        this.ipRegexPattern = regex;
    }

    public String getIPRegex() {
        return this.ipRegexPattern;
    }
}
