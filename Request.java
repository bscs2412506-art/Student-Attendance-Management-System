class Request {
    String requestId;
    Student student;
    Session session;
    AttendanceStatus requestedStatus;
    String reason;
    RequestStatus status;
    Request(String requestId, Student student, Session session, AttendanceStatus requestedStatus, String reason) {
        this.requestId = requestId; this.student = student; this.session = session;
        this.requestedStatus = requestedStatus; this.reason = reason; this.status = RequestStatus.PENDING;
    }

    public Request(String requestId, Student student, Session session, AttendanceStatus status, String reason, AttendanceStatus status1) {
    }
}