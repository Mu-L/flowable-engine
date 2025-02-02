/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.flowable.common.rest.exception;

import java.util.UUID;

import org.flowable.common.engine.api.FlowableException;
import org.flowable.common.engine.api.FlowableForbiddenException;
import org.flowable.common.engine.api.FlowableIllegalArgumentException;
import org.flowable.common.engine.api.FlowableIllegalStateException;
import org.flowable.common.engine.api.FlowableObjectNotFoundException;
import org.flowable.common.engine.api.FlowableTaskAlreadyClaimedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Base class for ExceptionHandlerAdvice controllers From http://spring.io/blog/2013/11/01/exception-handling-in-spring-mvc
 * 
 * @author Tijs Rademakers
 */
@ControllerAdvice
public class BaseExceptionHandlerAdvice {

    private static final Logger LOGGER = LoggerFactory.getLogger(BaseExceptionHandlerAdvice.class);

    /**
     * Flag indicating whether to send the full error exception message for unknown exceptions.
     * If set to {@code true}, then the {@link Exception#getMessage()} will be set on the {@link ErrorInfo#setException(String)},
     * otherwise a unique error identifier will be set and a message containing that identifier will be logged.
     */
    protected boolean sendFullErrorException = true;

    @ResponseStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE) // 415
    @ExceptionHandler(FlowableContentNotSupportedException.class)
    @ResponseBody
    public ErrorInfo handleNotSupported(FlowableContentNotSupportedException e) {
        return new ErrorInfo("Content is not supported", e);
    }

    @ResponseStatus(HttpStatus.CONFLICT) // 409
    @ExceptionHandler(FlowableConflictException.class)
    @ResponseBody
    public ErrorInfo handleConflict(FlowableConflictException e) {
        return new ErrorInfo("Conflict", e);
    }

    @ResponseStatus(HttpStatus.NOT_FOUND) // 404
    @ExceptionHandler(FlowableObjectNotFoundException.class)
    @ResponseBody
    public ErrorInfo handleNotFound(FlowableObjectNotFoundException e) {
        return new ErrorInfo("Not found", e);
    }

    @ResponseStatus(HttpStatus.FORBIDDEN) // 403
    @ExceptionHandler(FlowableForbiddenException.class)
    @ResponseBody
    public ErrorInfo handleForbidden(FlowableForbiddenException e) {
        return new ErrorInfo("Forbidden", e);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST) // 400
    @ExceptionHandler(FlowableIllegalArgumentException.class)
    @ResponseBody
    public ErrorInfo handleIllegalArgument(FlowableIllegalArgumentException e) {
        return new ErrorInfo("Bad request", e);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST) // 400
    @ExceptionHandler(FlowableIllegalStateException.class)
    @ResponseBody
    public ErrorInfo handleIllegalState(FlowableIllegalStateException e) {
        return new ErrorInfo("Bad request", e);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST) // 400
    @ExceptionHandler(HttpMessageConversionException.class)
    @ResponseBody
    public ErrorInfo handleBadMessageConversion(HttpMessageConversionException e) {
        if (sendFullErrorException) {
            return new ErrorInfo("Bad request", e);
        } else {
            String errorIdentifier = UUID.randomUUID().toString();
            LOGGER.warn("Invalid Message conversion exception. Error ID: {}", errorIdentifier, e);
            ErrorInfo errorInfo = new ErrorInfo("Bad request", null);
            errorInfo.setException("Invalid HTTP message. Error ID: " + errorIdentifier);
            return errorInfo;
        }
    }

    @ResponseStatus(HttpStatus.CONFLICT) // 409
    @ExceptionHandler(FlowableTaskAlreadyClaimedException.class)
    @ResponseBody
    public ErrorInfo handleTaskAlreadyClaimed(FlowableTaskAlreadyClaimedException e) {
        return new ErrorInfo("Task was already claimed", e);
    }

    // Fall back

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR) // 500
    @ExceptionHandler(Exception.class)
    @ResponseBody
    public ErrorInfo handleOtherException(Exception e) {
        if (sendFullErrorException) {
            LOGGER.error("Unhandled exception", e);
            return new ErrorInfo("Internal server error", e);
        } else {

            String errorIdentifier = UUID.randomUUID().toString();
            LOGGER.error("Unhandled exception. Error ID: {}", errorIdentifier, e);
            ErrorInfo errorInfo = new ErrorInfo("Internal server error", e);
            errorInfo.setException("Error with ID: " + errorIdentifier);
            return errorInfo;
        }
    }

    public boolean isSendFullErrorException() {
        return sendFullErrorException;
    }

    public void setSendFullErrorException(boolean sendFullErrorException) {
        this.sendFullErrorException = sendFullErrorException;
    }
}
