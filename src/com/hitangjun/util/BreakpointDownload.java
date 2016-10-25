package com.hitangjun.util;

import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.UUID;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class BreakpointDownload{

    protected transient final Logger log = LoggerFactory
            .getLogger(this.getClass());

    protected HttpServletRequest request;
    protected HttpServletResponse response;
    protected Map<String,Object> session;

    /**
     * 读取存储文件,支持断点续传
     *
     * http://192.168.1.8:8010/music/data.msp?fileName=4eb9e31fc21ce891397046d5.png
     */
    public String readFile(){

        long start = System.currentTimeMillis();
        //open RandomAccess file operator stream
        RandomAccessFile raf = new RandomAccessFile(filePath);
        BufferedOutputStream bos = null;
        String fileId = MD5(filePath);
        response.reset();
        response.resetBuffer();
        int bufferSize = 1024;
        response.setBufferSize(bufferSize);
        try {
            response.setContentType("image/"+extFileName);
            response.setHeader("Accept-Ranges", "bytes");

            if(raf != null){//文件读取成功
                long contentLength = raf.length();
                response.setHeader("Etag", "W/\""+contentLength+"-"+fileId+"\"");
                response.setHeader("Last-Modified", raf.lastModifiedTime);
                byte[] buffer = new byte[bufferSize];
                bos = new BufferedOutputStream(response.getOutputStream());
                long pos = 0;

                ArrayList ranges = null;

                long s = System.currentTimeMillis();
                // Parse range specifier
                ranges = parseRange(request, response, contentLength);
                long requestLength = 0;
                log.debug("parse range spent {}",System.currentTimeMillis()-s);
                if ( (((ranges == null) || (ranges.isEmpty()))
                        && (request.getHeader("Range") == null) )
                        || (ranges == FULL) ){
                    log.debug("========ranges is empty ,no need to skip,fileId {} , contentLength {}",new String[]{fileId,String.valueOf(contentLength)});

                    if (contentLength < Integer.MAX_VALUE) {
                        getResponse().setContentLength((int) contentLength);
                    } else {
                        // Set the content-length as String to be able to use a long
                        getResponse().setHeader("content-length", "" + contentLength);
                    }
                    requestLength = contentLength;
                }else{
                    // 若客户端传来Range，说明之前下载了一部分，设置206状态(SC_PARTIAL_CONTENT)
                    getResponse().setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);

                    log.debug("=======ranges.size() {}",ranges.size());
                    if (ranges.size() == 1) {

                        Range range = (Range) ranges.get(0);
                        response.addHeader("Content-Range", "bytes "
                                + range.start
                                + "-" + range.end + "/"
                                + range.length);
                        log.debug("Content-Range:  {}","bytes "
                                + range.start
                                + "-" + range.end + "/"
                                + range.length);

                        long length = range.end - range.start + 1;
                        if (length < Integer.MAX_VALUE) {
                            response.setContentLength((int) length);
                        } else {
                            // Set the content-length as String to be able to use a long
                            response.setHeader("content-length", "" + length);
                        }
                        log.debug("request range length = {}",length);
                        pos = range.start;
                        requestLength = length;
                    } else {
                        response.setContentType("multipart/byteranges; boundary="
                                + mimeSeparation);
                        log.warn("for multipart transfer TODO");
                    }
                }
                log.debug("skip pos = {}",pos);
                if (pos != 0) {
                    raf.seek(pos);
                }
                long ws = System.currentTimeMillis();
                if(requestLength < bufferSize){
                    bufferSize = (int)requestLength;
                }

                int readedLen = 0;
                for(int len = 0;len<requestLength;){
                    readedLen = raf.read(buffer,(int)pos,(int)requestLength);
                    bos.write(buffer,0,readedLen);
                    len+=readedLen;
                }

                bos.flush();
            }

        } catch (Exception e) {
            log.error("read error "+e);
        }finally{
            if(null != bos){
                try {
                    bos.close();
                } catch (Exception e) {
                    log.error("cloudStoreFile close file output stream error "+e);
                }
            }
        }
        return null;
    }

//   protected void printHeader(){
//	   log.debug("******************** headers ***********************");
//	  Enumeration headers = request.getHeaderNames();
//	   while(headers.hasMoreElements()){
//		   String name = (String)headers.nextElement();
//		   String value = request.getHeader(name);
//		   log.debug("{} = {}",new String[]{name,value});
//	   }
//	   log.debug("*****************************************************");
//   }

    /**
     * MIME multipart separation string
     */
    protected static final String mimeSeparation = "CATALINA_MIME_BOUNDARY";

    /**
     * Full range marker.
     */
    protected static ArrayList FULL = new ArrayList();

    /**
     * Parse the range header.
     *
     * @param request The servlet request we are processing
     * @param response The servlet response we are creating
     * @return Vector of ranges
     */
    protected ArrayList parseRange(HttpServletRequest request,
                                   HttpServletResponse response,long length)
            throws IOException {

        // Checking If-Range
        String headerValue = request.getHeader("If-Range");
        log.debug("headerValue = ",headerValue);
        if (headerValue != null) {

            long headerValueTime = (-1L);
            try {
                headerValueTime = request.getDateHeader("If-Range");
            } catch (IllegalArgumentException e) {
                ;
            }

            String eTag = "W/\""+length+"-1316052976000\"";
            long lastModified = 1316052976000L;

            log.debug("headerValueTime = ",headerValueTime);
            if (headerValueTime == (-1L)) {

                // If the ETag the client gave does not match the entity
                // etag, then the entire entity is returned.
                if (!eTag.equals(headerValue.trim())){
                    log.debug("If the ETag the client gave does not match the entity etag, then the entire entity is returned.");
                    return FULL;
                }
            } else {

                // If the timestamp of the entity the client got is older than
                // the last modification date of the entity, the entire entity
                // is returned.
                if (lastModified > (headerValueTime + 1000)){
                    log.debug("If the timestamp of the entity the client got is older than the last modification date of the entity, the entire entity is returned");
                    return FULL;

                }
            }

        }

        long fileLength = length;

        if (fileLength == 0)
            return null;

        // Retrieving the range header (if any is specified
        String rangeHeader = request.getHeader("Range");

        log.debug("rangeHeader = ",rangeHeader);
        if (rangeHeader == null){
            return null;
        }
        // bytes is the only range unit supported (and I don't see the point
        // of adding new ones).
        if (!rangeHeader.startsWith("bytes")) {
            response.addHeader("Content-Range", "bytes */" + fileLength);
            response.sendError
                    (HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
            return null;
        }

        rangeHeader = rangeHeader.substring(6);

        // Vector which will contain all the ranges which are successfully
        // parsed.
        ArrayList<Range> result = new ArrayList<Range>();
        StringTokenizer commaTokenizer = new StringTokenizer(rangeHeader, ",");

        // Parsing the range list
        while (commaTokenizer.hasMoreTokens()) {
            String rangeDefinition = commaTokenizer.nextToken().trim();

            Range currentRange = new Range();
            currentRange.length = fileLength;

            int dashPos = rangeDefinition.indexOf('-');

            if (dashPos == -1) {
                response.addHeader("Content-Range", "bytes */" + fileLength);
                response.sendError
                        (HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
                return null;
            }

            if (dashPos == 0) {

                try {
                    long offset = Long.parseLong(rangeDefinition);
                    currentRange.start = fileLength + offset;
                    currentRange.end = fileLength - 1;
                } catch (NumberFormatException e) {
                    response.addHeader("Content-Range",
                            "bytes */" + fileLength);
                    response.sendError
                            (HttpServletResponse
                                    .SC_REQUESTED_RANGE_NOT_SATISFIABLE);
                    return null;
                }

            } else {

                try {
                    currentRange.start = Long.parseLong
                            (rangeDefinition.substring(0, dashPos));
                    if (dashPos < rangeDefinition.length() - 1)
                        currentRange.end = Long.parseLong
                                (rangeDefinition.substring
                                        (dashPos + 1, rangeDefinition.length()));
                    else
                        currentRange.end = fileLength - 1;
                } catch (NumberFormatException e) {
                    response.addHeader("Content-Range",
                            "bytes */" + fileLength);
                    response.sendError
                            (HttpServletResponse
                                    .SC_REQUESTED_RANGE_NOT_SATISFIABLE);
                    return null;
                }

            }

            if (!currentRange.validate()) {
                response.addHeader("Content-Range", "bytes */" + fileLength);
                response.sendError
                        (HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
                return null;
            }

            result.add(currentRange);
        }

        return result;
    }

    // ------------------------------------------------------ Range Inner Class


    protected class Range {

        public long start;
        public long end;
        public long length;

        /**
         * Validate range.
         */
        public boolean validate() {
            if (end >= length)
                end = length - 1;
            return ( (start >= 0) && (end >= 0) && (start <= end)
                    && (length > 0) );
        }

        public void recycle() {
            start = 0;
            end = 0;
            length = 0;
        }

    }

}

