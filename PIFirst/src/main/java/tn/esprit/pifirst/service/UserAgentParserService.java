package tn.esprit.pifirst.service;


import nl.basjes.parse.useragent.UserAgent;
import nl.basjes.parse.useragent.UserAgentAnalyzer;
import org.springframework.stereotype.Service;

@Service
public class UserAgentParserService {

    private final UserAgentAnalyzer userAgentAnalyzer;

    public UserAgentParserService() {
        this.userAgentAnalyzer = UserAgentAnalyzer.newBuilder()
                .hideMatcherLoadStats()
                .withCache(10000)
                .build();
    }

    public ParsedUserAgent parse(String userAgentString) {
        if (userAgentString == null || userAgentString.isEmpty()) {
            ParsedUserAgent empty = new ParsedUserAgent();
            empty.setDeviceType("desktop");
            empty.setOs("unknown");
            empty.setBrowser("unknown");
            return empty;
        }

        UserAgent agent = userAgentAnalyzer.parse(userAgentString);

        ParsedUserAgent result = new ParsedUserAgent();
        result.setDeviceType(getDeviceType(agent.getValue("DeviceClass")));
        result.setOs(agent.getValue("OperatingSystemName"));
        result.setBrowser(agent.getValue("AgentName"));

        return result;
    }

    private String getDeviceType(String deviceClass) {
        if (deviceClass == null) return "desktop";
        if (deviceClass.contains("Phone")) return "mobile";
        if (deviceClass.contains("Tablet")) return "tablet";
        return "desktop";
    }

    public static class ParsedUserAgent {
        private String deviceType;
        private String os;
        private String browser;

        public String getDeviceType() { return deviceType; }
        public void setDeviceType(String deviceType) { this.deviceType = deviceType; }
        public String getOs() { return os; }
        public void setOs(String os) { this.os = os; }
        public String getBrowser() { return browser; }
        public void setBrowser(String browser) { this.browser = browser; }
    }
}