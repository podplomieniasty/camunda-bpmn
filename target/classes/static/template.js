function getCardTemplate(obj) {
    return `<div class="card" style="width: 18rem;">
      <img src="https://store-images.s-microsoft.com/image/apps.46116.70628353720390187.c5ec2284-1a6e-4ed0-a094-b54b14b8d466.f01d3b8d-41e1-42bc-b322-443ee5b1f390?q=90&w=480&h=270" class="card-img-top" alt="...">
      <div class="card-body bg-light-subtle">
        <h5 class="card-title">${obj.title}</h5>
        <p class="card-text">${obj.genre}, ${obj.duration / 60}h${obj.duration % 60 + 'm' || ''}</p>
        <a href="#" class="btn btn-primary">Zarezerwuj bilet</a>
      </div>
    </div>`
}

