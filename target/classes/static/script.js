let processInstanceKey = '';

const API_URL = 'http://localhost:8080/api';
const CAMUNDA_URL = 'http://localhost:8080/camunda'

const DIV_MOVIES = document.getElementById('movies-wrapper');
const MODAL = document.getElementById('my-modal');

let MOVIE_DATE = document.getElementById('movie-date');
let MOVIE_HOUR = document.getElementById('movie-hour');
let MOVIE_SEAT = document.getElementById('movie-seat');
let U_FNAME = document.getElementById('user-fname');
let U_LNAME = document.getElementById('user-lname');
let U_EMAIL = document.getElementById('user-email');

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
                movies.forEach(mov => {
                    document.getElementById(`card-${mov.id}`).addEventListener('click', () => {
                        toggleModal(mov);
                        document.getElementById('send-data').addEventListener('click', () => {
                            resetGlobalVariables();
                            const obj = {
                                user_fname: U_FNAME.value,
                                user_lname: U_LNAME.value,
                                user_email: U_EMAIL.value,
                                movie_date: MOVIE_DATE.value,
                                movie_hour: MOVIE_HOUR.value,
                                movie_seat: MOVIE_SEAT.value,
                                movie_title: mov.title,
                                movie_id: mov.id,
                                movie_genre: mov.genre,
                                movie_duration: mov.duration
                            }
                            startProcess(obj);
                        });
                    }, false);
                })
            })
})

function toggleModal(obj) {
    MODAL.style.display = 'flex';
    MODAL.style.visibility = 'visible';
    MODAL.innerHTML = getModalTemplate(obj);

    resetGlobalVariables();

    fetch(`/api/showing?movie=${obj.id}`, {
        method: 'GET',
    })
    .then(res => res.json())
    .then(showings => {
        showings.forEach(showing => {
            MOVIE_DATE.innerHTML += generateSelectOptions(showing.date);
        });
        MOVIE_DATE.addEventListener('change', () => {
            MOVIE_HOUR.innerHTML = '';
            MOVIE_SEAT.innerHTML = '';

            showings.filter((o) => o.date === MOVIE_DATE.value).forEach(showing => {
                MOVIE_HOUR.innerHTML += generateSelectOptions(showing.hour);
            })
            const showing = showings.find((o) => o.date === MOVIE_DATE.value && o.hour === MOVIE_HOUR.value);

            // fetching seats
            fetch(`/api/room?room=${showing.cinemaRoomId}`, { method: 'GET' })
            .then(res => res.json()).then(room => {
                for(let c = 1; c <= room.columns; c++) {
                    for(let r = 1; r <= room.rows; r++) {
                        MOVIE_SEAT.innerHTML += generateSelectOptions(`C${c}-R${r}`);
                    }
                }
            })
            
            MOVIE_HOUR.addEventListener('change', () => {
                MOVIE_SEAT.innerHTML = '';
                const showing = showings.find((o) => o.date === MOVIE_DATE.value && o.hour === MOVIE_HOUR.value);
                // fetching seats
                fetch(`/api/room?room=${showing.cinemaRoomId}`, { method: 'GET' })
                .then(res => res.json()).then(room => {
                    for(let c = 1; c <= room.columns; c++) {
                        for(let r = 1; r <= room.rows; r++) {
                            MOVIE_SEAT.innerHTML += generateSelectOptions(`C${c}-R${r}`);
                        }
                    }
                })
            })
        })
    })
}

function startProcess(obj) {

    fetch(`/api/showing?movie=${obj.movie_id}`, {
        method: 'GET',
    })
    .then(res => res.json())
    .then(showings => {
        let id = showings.find((o) => o.date === MOVIE_DATE.value && o.hour === MOVIE_HOUR.value).id;
        fetch(`/camunda/start`, { 
            method: 'POST',
            headers: {'Content-Type': 'application/json'}, 
            body: JSON.stringify({...obj, showingId: id})})
        .then(res => res.json())
        .then(variables => {
            processInstanceKey = variables.processInstanceKey;
            console.log(processInstanceKey);
            const eventSource = new EventSource(`/camunda/subscribe?processInstanceKey=${processInstanceKey}`)
            eventSource.onmessage = async function (event) {
                try {
                    console.log('Camunda: ', event);
                } catch (err) {
                    console.error("Error caught in onmessage: ", err);
                }
            }
        })
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

function resetGlobalVariables() {
    U_FNAME = document.getElementById('user-fname');
    U_LNAME = document.getElementById('user-lname');
    U_EMAIL = document.getElementById('user-email');
    MOVIE_DATE = document.getElementById('movie-date');
    MOVIE_HOUR = document.getElementById('movie-hour');
    MOVIE_SEAT = document.getElementById('movie-seat');
}