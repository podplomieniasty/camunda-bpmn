document.getElementById("cancelReservationForm").addEventListener("submit", async function (event) {
    event.preventDefault();

    const accessCode = document.getElementById("accessCode").value;
    const email = document.getElementById("email").value;

    const data = {
        accessCode: accessCode,
        email: email,
    };

    try {
        const response = await fetch("http://localhost:8080/camunda/cancel", {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
            },
            body: JSON.stringify(data),
        });

        const responseData = await response.json();
        const processInstanceKey = responseData.processInstanceKey;

        if (!processInstanceKey) {
            throw new Error("No processInstanceKey returned in the response");
        }

        console.log("Process Instance Key:", processInstanceKey);

        const eventSource = new EventSource(`/camunda/subscribe?processInstanceKey=${processInstanceKey}`);
        eventSource.onmessage = async function (event) {
            try {
                console.log("Camunda Event: ", event.data);
                switch (event.data) {
                    case "INVALID_DATA":
                        alert("Podano błędny kod dostępu lub adres e-mail. Sprawdź wprowadzone dane i spróbuj ponownie.");
                        break;
                    case "RESERVATION_CANCELLED_SUCCESS":
                        window.location.href = "cancellation_success.html";
                        break;
                    case "RESERVATION_CANCELLED_FAILED":
                        window.location.href = "cancellation_failed.html";
                        break;
                    default:
                        console.log("Unknown event:", event.data);
                }
            } catch (err) {
                console.error("Error caught in onmessage: ", err);
            }
        };

        eventSource.onerror = function (err) {
            console.error("EventSource error: ", err);
            eventSource.close();
        };

    } catch (error) {
        console.error("Error:", error);
        document.getElementById("responseMessage").textContent = "An error occurred while starting the process.";
    }
});
