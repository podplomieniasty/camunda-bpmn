let processInstanceKey = '';

const API_URL = 'http://localhost:8080/api';
const CAMUNDA_URL = 'http://localhost:8080/camunda'

const DIV_MOVIES = document.getElementById('movies-wrapper');

document.addEventListener("DOMContentLoaded", () => {
    /*
        Fetch movies, prepare cards to choose
    */
    fetch(`${API_URL}/movies`, {
            method: 'GET',
        })
            .then((res) => res.json())
            .then((movies) => {
                movies.forEach(mov => {
                    DIV_MOVIES.innerHTML += getCardTemplate(mov);
                });
            })
})



function startProcess() {
    fetch(`/camunda/start`, { 
        method: 'POST',
        headers: {'Content-Type': 'application/json'}, 
        body: JSON.stringify({
        variable1: 'This is a test. If this works, then good'
    })})
    .then(res => res.json())
    .then(variables => {
        processInstanceKey = variables.processInstanceKey;
        const eventSource = new EventSource(`/api/subscribe?processInstanceKey=${processInstanceKey}`);
        eventSource.onmessage = async function(e) {
            console.log(e.data);
        }
    })
}

function fetchMovies() {
    fetch(`${API_URL}/movies`, {
        method: 'GET',
    })
        .then((res) => res.json())
        .then((movies) => {
            movies.forEach(mov => {
            });
        })
}

