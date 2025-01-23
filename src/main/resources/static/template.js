function getCardTemplate(obj) {
    return `<div class="card" style="width: 18rem;">
      <img src="https://store-images.s-microsoft.com/image/apps.46116.70628353720390187.c5ec2284-1a6e-4ed0-a094-b54b14b8d466.f01d3b8d-41e1-42bc-b322-443ee5b1f390?q=90&w=480&h=270" class="card-img-top" alt="...">
      <div class="card-body bg-light-subtle">
        <h5 class="card-title">${obj.title}</h5>
        <p class="card-text">${obj.genre}, ${obj.duration / 60}h${obj.duration % 60 + 'm' || ''}</p>
        <button id="card-${obj.id}" class="btn btn-primary">Zarezerwuj bilet</button>
      </div>
    </div>`
}

function exit() {
  console.log('Toggling off visibility');
  MODAL.style.visibility = 'hidden';
}

function getModalTemplate(obj, dates, seats) {
  
  return `<div class="modal-wrapper">
      <h3>Rezerwacja biletów</h3>
      <button type="button" id="modal-close" onclick="exit()">X</button>
      <table class="modal-table">
        <tr>
          <td>Bilet na:</td><td>${obj.title}</td>
        </tr>
        <tr>
          <td>Gatunek:</td><td>${obj.genre}</td>
        </tr>
        <tr>
          <td>Czas trwania:</td><td>${obj.duration / 60}h${obj.duration % 60 + 'm' || ''}</td>
        </tr>
        <tr>
          <td>Data seansu:</td>
          <td>
            <select name="movie-date" id="movie-date">
              <option value="24.01.2025">24.01.2025</option>
              <option value="25.01.2025">25.01.2025</option>
            </select>
          </td>
        </tr>
        <tr>
          <td>Godzina seansu:</td>
          <td>
            <select name="movie-hour" id="movie-hour" >
              <option value="A-1">15:00</option>
              <option value="A-2">17:30</option>
            </select>
          </td>
        </tr>
        <tr>
          <td>Miejsce siedzące</td>
          <td>
            <select name="movie-seat" id="movie-seat" >
              <option value="A-1">A-1</option>
              <option value="A-2">A-2</option>
              <option value="B-1">B-1</option>
              <option value="B-2">B-2</option>
            </select>
          </td>
        </tr>
      </table>
      
      <h4>Dane użytkownika</h4>
      <table class="modal-table">
        <tr>
          <td>Imię:</td><td><input type="text" id="user-fname"/></td>
        </tr>
        <tr>
          <td>Nazwisko:</td><td><input type="text" id="user-lname"/></td>
        </tr>
        <tr>
          <td>Adres e-mail<span style="font-weight: bold; color: red;">*</span>:</td><td><input required ="text" id="user-email"/></td>
        </tr>
      </table>
      <p>Na podany adres e-mail wyślemy Twój kod rezerwacji.</p>
      <button type="button" id="send-data">Zarezerwuj miejsce</button>
    </div>`
}