let index = 1;
let productContainer;

document.addEventListener("DOMContentLoaded", () => {
    productContainer = document.getElementById("product-container");
});


function addProductBlock() {
    const block = document.createElement("div");
    block.className = "product-block card p-4 mt-6";
    block.innerHTML = `
            <div class="grid grid-cols-1 gap-4">
                <input type="text" name="products[${index}].name" class="form-control" placeholder="Название" required />
                <textarea name="products[${index}].description" class="form-control" placeholder="Описание" required></textarea>
                <input type="number" step="0.01" name="products[${index}].price" class="form-control" placeholder="Цена" required />
                <input type="text" name="products[${index}].imageUrl" class="form-control" placeholder="Ссылка на изображение" required />
            </div>
        `;
    productContainer.appendChild(block);
    index++;
}