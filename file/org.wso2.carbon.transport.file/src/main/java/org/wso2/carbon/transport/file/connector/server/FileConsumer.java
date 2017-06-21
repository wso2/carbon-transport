/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.transport.file.connector.server;

import org.apache.commons.vfs2.FileNotFoundException;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.RandomAccessContent;
import org.apache.commons.vfs2.impl.StandardFileSystemManager;
import org.apache.commons.vfs2.provider.UriParser;
import org.apache.commons.vfs2.provider.ftp.FtpFileSystemConfigBuilder;
import org.apache.commons.vfs2.util.RandomAccessMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.messaging.BinaryCarbonMessage;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.messaging.CarbonMessageProcessor;
import org.wso2.carbon.messaging.TextCarbonMessage;
import org.wso2.carbon.messaging.exceptions.ServerConnectorException;
import org.wso2.carbon.transport.file.connector.server.exception.FileServerConnectorException;
import org.wso2.carbon.transport.file.connector.server.util.Constants;
import org.wso2.carbon.transport.file.connector.server.util.FileTransportUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Provides the capability to process a file and delete it afterwards.
 */
public class FileConsumer {

    private static final Logger log = LoggerFactory.getLogger(FileConsumer.class);

    private Map<String, String> fileProperties;
    private FileSystemManager fsManager = null;
    private CarbonMessageProcessor messageProcessor;
    private String fileURI;
    private String serviceName;
    private FileObject fileObject;
    private FileSystemOptions fso;

    private final byte[] inbuf = new byte[4096];
    private int seek;
    private boolean reOpen = true;
    private long currentTime = 0L;
    private long position = 0L;
    private RandomAccessContent reader = null;
    private int throttle;

    public FileConsumer(String id, Map<String, String> fileProperties,
                        CarbonMessageProcessor messageProcessor)
            throws ServerConnectorException {
        this.serviceName = id;
        this.fileProperties = fileProperties;
        this.messageProcessor = messageProcessor;

        setupParams();
        try {
            StandardFileSystemManager fsm = new StandardFileSystemManager();
            fsm.setConfiguration(getClass().getClassLoader().getResource("providers.xml"));
            fsm.init();
            fsManager = fsm;
        } catch (FileSystemException e) {
            throw new ServerConnectorException("Could not initialize File System Manager from " +
                    "the configuration: providers.xml", e);
        }
        Map<String, String> options = parseSchemeFileOptions(fileURI);
        fso = FileTransportUtils.attachFileSystemOptions(options, fsManager);

        if (options != null && Constants.SCHEME_FTP.equals(options.get(Constants.SCHEME))) {
            FtpFileSystemConfigBuilder.getInstance().setPassiveMode(fso, true);
        }

        try {
            fileObject = fsManager.resolveFile(fileURI, fso);
            reader = fileObject.getContent().getRandomAccessContent(RandomAccessMode.READ);
            position = this.seek == -1 ? this.fileObject.getContent().getSize() : seek;
            currentTime = System.currentTimeMillis();
            reader.seek(position);
        } catch (FileSystemException e) {
            throw new FileServerConnectorException("Failed to resolve fileURI: "
                    + FileTransportUtils.maskURLPassword(fileURI), e);
        } catch (IOException e) {
            throw new FileServerConnectorException("Failed to read File: "
                                                   + FileTransportUtils.maskURLPassword(fileURI), e);
        }
    }

    /**
     * Do the file processing operation for the given set of properties. Do the
     * checks and pass the control to processFile method
     */
    public void consume() throws FileServerConnectorException {
        if (log.isDebugEnabled()) {
            log.debug("Polling for file : " + FileTransportUtils.maskURLPassword(fileURI));
        }

        // If file/folder found proceed to the processing stage
        boolean isFileExists;
        try {
            isFileExists = fileObject.exists();
        } catch (FileSystemException e) {
            throw new FileServerConnectorException("Error occurred when determining whether the file at URI : " +
                                                   FileTransportUtils.maskURLPassword(fileURI) + " exists. " + e);
        }

        boolean isFileReadable;
        try {
            isFileReadable = fileObject.isReadable();
        } catch (FileSystemException e) {
            throw new FileServerConnectorException("Error occurred when determining whether the file at URI : " +
                                                   FileTransportUtils.maskURLPassword(fileURI) + " is readable. " + e);
        }

        if (isFileExists && isFileReadable) {
            FileType fileType;
            try {
                fileType = fileObject.getType();
            } catch (FileSystemException e) {
                throw new FileServerConnectorException(
                        "Error occurred when determining whether file: " + FileTransportUtils.maskURLPassword(fileURI) +
                        " is a file or a folder", e);
            }

            if (fileType == FileType.FILE) {
                processFile(fileObject);
            } else {
                throw new FileServerConnectorException(
                        "Unable to access or read file or directory : " + FileTransportUtils.maskURLPassword(fileURI));
            }
            if (log.isDebugEnabled()) {
                log.debug("End : Scanning directory or file : " + FileTransportUtils.maskURLPassword(fileURI));
            }
        }
    }



    /**
     * Setup the required transport parameters.
     */
    private void setupParams() throws ServerConnectorException {
        fileURI = fileProperties.get(Constants.TRANSPORT_FILE_FILE_URI);
        if (fileURI == null) {
            throw new ServerConnectorException(Constants.TRANSPORT_FILE_FILE_URI + " is a " +
                    "mandatory parameter for " + Constants.PROTOCOL_FILE + " transport.");
        }
        if (fileURI.trim().equals("")) {
            throw new ServerConnectorException(Constants.TRANSPORT_FILE_FILE_URI + " parameter " +
                    "cannot be empty for " + Constants.PROTOCOL_FILE + " transport.");
        }
        String seek = fileProperties.get(Constants.SEEK);
        if (seek != null) {
            this.seek = Integer.parseInt(seek);
        } else {
            this.seek = -1;
        }
        String throttle = fileProperties.get(Constants.THROTTLE);
        if (throttle != null) {
            this.throttle = Integer.parseInt(throttle);
        } else {
            this.throttle = -1;
        }
    }

    private Map<String, String> parseSchemeFileOptions(String fileURI) {
        String scheme = UriParser.extractScheme(fileURI);
        if (scheme == null) {
            return null;
        }
        HashMap<String, String> schemeFileOptions = new HashMap<>();
        schemeFileOptions.put(Constants.SCHEME, scheme);
        addOptions(scheme, schemeFileOptions);
        return schemeFileOptions;
    }

    private void addOptions(String scheme, Map<String, String> schemeFileOptions) {
        if (scheme.equals(Constants.SCHEME_SFTP)) {
            for (Constants.SftpFileOption option : Constants.SftpFileOption.values()) {
                String strValue = fileProperties.get(Constants.SFTP_PREFIX + option.toString());
                if (strValue != null && !strValue.equals("")) {
                    schemeFileOptions.put(option.toString(), strValue);
                }
            }
        }
    }

    /**
     * Actual processing of the file/folder.
     *
     * @param file
     * @return
     */
    private FileObject processFile(FileObject file) throws FileServerConnectorException {

        try {
            boolean newer = isFileNewer(fileObject, currentTime);
            long length = this.fileObject.getContent().getSize();
            if (length < position) {

                EventListener.fileRotated(fileObject, messageProcessor, serviceName);
                try {
                    reader = fileObject.getContent().getRandomAccessContent(RandomAccessMode.READ);
                    position = fileObject.getContent().getSize();
                } catch (FileNotFoundException e) {
                    throw new FileServerConnectorException("File Not Found: " +
                                                           FileTransportUtils.maskURLPassword(fileURI), e);
                }
            } else {
                if (length > position) {
                    position = this.readLines(reader);
                    currentTime = System.currentTimeMillis();
                } else if (newer) {
                    position = fileObject.getContent().getSize();
                    reader.seek(position);
                    position = this.readLines(reader);
                    currentTime = System.currentTimeMillis();
                }

                if (this.reOpen) {
                    FileObject parent = fileObject.getParent();
                    parent.getType(); // assure that parent folder is attached
                    parent.refresh();
                    fileObject.refresh();
                    reader.close();
                    reader = fileObject.getContent().getRandomAccessContent(RandomAccessMode.READ);
                    reader.seek(position);
                }
            }
        } catch (FileSystemException e) {
            throw new FileServerConnectorException(
                    "Error in reading file: " + FileTransportUtils.maskURLPassword(fileURI), e);
        } catch (IOException e) {
            throw new FileServerConnectorException(
                    "Error in reading file: " + FileTransportUtils.maskURLPassword(fileURI), e);
        }
        return file;
    }

    private long readLines(RandomAccessContent reader)
            throws IOException, FileServerConnectorException {

        long pos = reader.getFilePointer();
        long rePos = pos;

        List<Byte> list = new ArrayList<>();
        int num;
        int lines = 0;
        boolean throttled = false;
            for (;
                 ((num = read(reader, inbuf)) != -1) && !throttled; pos = reader.getFilePointer()) {
                for (int i = 0; (i < num) && !throttled; ++i) {
                    byte ch = this.inbuf[i];
                    if (ch == 10) {
                        Byte[] line = new Byte[list.size()];
                        line = list.toArray(line);
                        EventListener.handle(line, messageProcessor, serviceName);
                        lines++;
                        list.clear();
                        rePos = pos + (long) i + 1L;
                    } else {
                        list.add(ch);
                    }
                    if (throttle != -1 && (lines > throttle) && ch == 10) {
                        throttled = true;
                    }
                }
            }

        reader.seek(rePos);
        return rePos;
    }
    private static boolean isFileNewer(FileObject file, long timeMillis) throws FileSystemException {
        if (file == null) {
            throw new IllegalArgumentException("No specified file");
        } else {
            return !file.exists() || file.getContent().getLastModifiedTime() > timeMillis;
        }
    }

    private static int read(RandomAccessContent reader, byte[] inbuf) throws IOException {
        InputStream is = reader.getInputStream();
        int count  = is.read(inbuf);
        return count;
    }

    private static class EventListener {

        private static void fileRotated(FileObject file, CarbonMessageProcessor messageProcessor, String serviceName)
                throws FileServerConnectorException {

            try {

                TextCarbonMessage cMessage = new TextCarbonMessage(file.getURL().toString());
                cMessage.setProperty(org.wso2.carbon.messaging.Constants.PROTOCOL, Constants.PROTOCOL_FILE);
                cMessage.setProperty(Constants.FILE_TRANSPORT_PROPERTY_SERVICE_NAME, serviceName);
                cMessage.setProperty(Constants.FILE_TRANSPORT_EVENT_NAME, Constants.FILE_ROTATE);

                messageProcessor.receive(cMessage, null);
            } catch (Exception e) {
                throw new FileServerConnectorException("Failed to send event message processor. ", e);
            }
        }

        private static void handle(Byte[] content, CarbonMessageProcessor messageProcessor, String serviceName)
                throws FileServerConnectorException {
            try {
                CarbonMessage cMessage = new BinaryCarbonMessage(ByteBuffer.wrap(toPrimitives(content)), true);
                cMessage.setProperty(org.wso2.carbon.messaging.Constants.PROTOCOL, Constants.PROTOCOL_FILE);
                cMessage.setProperty(Constants.FILE_TRANSPORT_PROPERTY_SERVICE_NAME, serviceName);
                cMessage.setProperty(Constants.FILE_TRANSPORT_EVENT_NAME, Constants.FILE_UPDATE);
                cMessage.setProperty(Constants.SINGLE_THREADED_EXECUTION, serviceName);

                messageProcessor.receive(cMessage, null);
            } catch (Exception e) {
                throw new FileServerConnectorException("Failed to send event message processor. ", e);
            }
        }

        private static byte[] toPrimitives(Byte[] oBytes) {

            byte[] bytes = new byte[oBytes.length];

            for (int i = 0; i < oBytes.length; i++) {
                bytes[i] = oBytes[i];
            }

            return bytes;
        }

    }

}
