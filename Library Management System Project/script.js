function getBooks() {
  return JSON.parse(localStorage.getItem("books")) || [];
}

function saveBooks(books) {
  localStorage.setItem("books", JSON.stringify(books));
}

/* ➕ ADD BOOK */
function addBook() {
  let title = document.getElementById("title").value;
  let author = document.getElementById("author").value;

  if (!title || !author) {
    alert("Please fill all fields!");
    return;
  }

  let books = getBooks();
  books.push({ title, author });

  saveBooks(books);

  alert("Book Added Successfully!");
  document.getElementById("title").value = "";
  document.getElementById("author").value = "";
}

/* 📖 DISPLAY BOOKS (VIEW PAGE) */
function displayBooks(list = null) {
  let books = list || getBooks();
  let table = document.getElementById("bookList");

  if (!table) return;

  table.innerHTML = "";

  books.forEach((book, index) => {
    table.innerHTML += `
      <tr>
        <td>${book.title}</td>
        <td>${book.author}</td>
        <td>
          <button onclick="editBook(${index})">Edit</button>
          <button onclick="deleteBook(${index})">Delete</button>
        </td>
      </tr>
    `;
  });
}

/* ✏️ EDIT BOOK */
function editBook(index) {
  let books = getBooks();
  let book = books[index];

  let newTitle = prompt("Edit Title:", book.title);
  let newAuthor = prompt("Edit Author:", book.author);

  if (newTitle && newAuthor) {
    books[index] = {
      title: newTitle,
      author: newAuthor
    };

    saveBooks(books);
    displayBooks();
  }
}

/* 🗑 DELETE BOOK (MAIN) */
function deleteBook(index) {
  let books = getBooks();

  books.splice(index, 1);

  saveBooks(books);
  displayBooks();
}

/* 🔍 SEARCH BOOK (VIEW PAGE) */
function searchBooks() {
  let query = document.getElementById("searchInput").value.toLowerCase();
  let books = getBooks();

  let filtered = books.filter(book =>
    book.title.toLowerCase().includes(query) ||
    book.author.toLowerCase().includes(query)
  );

  displayBooks(filtered);
}

/* =========================
   DELETE PAGE FUNCTIONS
   ========================= */

/* 📄 DISPLAY DELETE PAGE LIST */
function displayDeleteBooks(list = null) {
  let books = list || getBooks();
  let table = document.getElementById("deleteList");

  if (!table) return;

  table.innerHTML = "";

  books.forEach((book, index) => {
    table.innerHTML += `
      <tr>
        <td>${book.title}</td>
        <td>${book.author}</td>
        <td>
          <button onclick="deleteBook(${index})">Delete</button>
        </td>
      </tr>
    `;
  });
}

/* 🔍 SEARCH IN DELETE PAGE */
function searchDeleteBooks() {
  let query = document.getElementById("searchDelete").value.toLowerCase();
  let books = getBooks();

  let filtered = books.filter(book =>
    book.title.toLowerCase().includes(query) ||
    book.author.toLowerCase().includes(query)
  );

  displayDeleteBooks(filtered);
}