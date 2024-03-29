package vn.iotstar.jobhub_hcmute_be.exception;

public final class AlreadyExistException extends RuntimeException {


    public AlreadyExistException() {
        super();
    }

    public AlreadyExistException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public AlreadyExistException(final String message) {
        super(message);
    }

    public AlreadyExistException(final Throwable cause) {
        super(cause);
    }

}
