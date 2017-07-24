package org.wso2.carbon.transport.email.config.generator;

import org.wso2.carbon.config.annotation.Configuration;
import org.wso2.carbon.config.annotation.Element;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chathurika on 7/20/17.
 */

@Configuration(namespace = "wso2.transport.email", description = "Parent configuration")
public class EmailTransportConfiguration {


    @Element(description = "smtp server configurations")
    private List<SmtpServerConfiguration> smtpServerConfigurations = new ArrayList<>();

    @Element(description = "Imap server configurations")
    private List<ImapServerConfiguration> imapServerConfigurations = new ArrayList<>();

    @Element(description = "Imap server configurations")
    private List<Pop3ServerConfiguration> pop3ServerConfigurations = new ArrayList<>();

    public EmailTransportConfiguration() {
        SmtpServerConfiguration smtpServerConfiguration1 = new SmtpServerConfiguration();
        smtpServerConfiguration1.setName("mail.smtp.connectiontimeout");
        smtpServerConfiguration1.setValue("10000");
        smtpServerConfigurations.add(smtpServerConfiguration1);
        SmtpServerConfiguration smtpServerConfiguration2 = new SmtpServerConfiguration();
        smtpServerConfiguration2.setName("mail.smtp.timeout");
        smtpServerConfiguration2.setValue("10000");
        smtpServerConfigurations.add(smtpServerConfiguration2);

        Pop3ServerConfiguration pop3ServerConfiguration1 = new Pop3ServerConfiguration();
        pop3ServerConfiguration1.setName("mail.pop3.connectiontimeout");
        pop3ServerConfiguration1.setValue("10000");
        pop3ServerConfigurations.add(pop3ServerConfiguration1);

        ImapServerConfiguration imapServerConfiguration1 = new ImapServerConfiguration();
        imapServerConfiguration1.setName("mail.pop3.connectiontimeout");
        imapServerConfiguration1.setValue("10000");
        imapServerConfigurations.add(imapServerConfiguration1);


    }

    public void setSmtpServerConfigurations(List smtpServerConfigurations) {
        this.smtpServerConfigurations = smtpServerConfigurations;
    }

    public void setPop3ServerConfigurations(List pop3ServerConfigurations) {
        this.pop3ServerConfigurations = pop3ServerConfigurations;
    }

    public void setImapServerCoonfigurations(List imapServerConfigurations) {
         this.imapServerConfigurations = imapServerConfigurations;
    }

    public List getSmtpServerConfigurations() {
        return smtpServerConfigurations;
    }

    public List getPop3ServerConfigurations() {
        return pop3ServerConfigurations;
    }

    public List getImapServerCoonfigurations() {
        return imapServerConfigurations;
    }

}
