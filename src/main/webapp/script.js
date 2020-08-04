// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

function openFamilyForm() {
    document.getElementById("createFamilyForm").style.visibility = "visible";
}

function closeFamilyForm() {
    console.log("Close family");
    document.getElementById("createFamilyForm").style.visibility = "hidden";
}

function openNewMemberForm() {
    console.log("new member");
    document.getElementById("newMemberForm").style.visibility = "visible";
}

function closeNewMemberForm() {
  document.getElementById("newMemberForm").style.visibility = "hidden";
}

function openRemoveMemberForm() {
    document.getElementById("removeMemberForm").style.visibility = "visible";
}

function closeRemoveMemberForm() {
  document.getElementById("removeMemberForm").style.visibility = "hidden";
}

function loadFamilyMembers() {
  fetch('/family').then(response => response.json()).then((family) => {
    const familyElement = document.getElementById('family-container');
    familyElement.innerHTML = "";
    const familyHeader = document.createElement("HEADER");
    if(!("name" in family)) {
        familyHeader.innerText = "You are not in a family currently";
        familyElement.appendChild(familyHeader);
        document.getElementById('addMemberButton').setAttribute("style","visibility:hidden");
        document.getElementById('removeMemberButton').setAttribute("style","visibility:hidden");
        return;
    }
    familyHeader.innerText = "Current Family Members in " + family.name + ":";
    familyElement.appendChild(familyHeader);
    family.members.forEach((memberEmail) => {
      const memberListElement = document.createElement('li');
      memberListElement.innerText = memberEmail;
      familyElement.appendChild(memberListElement);
    })
    document.getElementById('addMemberButton').setAttribute("style","visibility:visible");
    document.getElementById('removeMemberButton').setAttribute("style","visibility:visible");
  });
}

function submitFamilyForm(formName, endpoint) {
    const form = document.getElementById(formName);

    // creating FormData to get the values of the form
    const formData = new FormData(form);
    const params = new URLSearchParams();

    // loop through the key and values of the form and add them to an array
    for (var pair of formData.entries()) {
        var key = pair[0];
        var value = pair[1];
        params.append(key, value);  
    }

    form.reset();

    fetch(new Request(endpoint, {method: 'POST', body: params, }))
        .then((response) => handleErrors(response)).then(() => {
            location.reload();
        }).catch(error => alert(error.message)); 
}

function createFamily() {
    submitFamilyForm('createFamilyForm', '/family');
    closeFamilyForm();
}

function addNewMember() {
    submitFamilyForm('newMemberForm', '/new-member');
    closeNewMemberForm();
}

function removeMember() {
    submitFamilyForm('removeMemberForm', '/delete-member');
    closeRemoveMemberForm();
}

function userLogin() {
  fetch('/login').then(response => response.text())
  .then((message) => {
    document.getElementById('login-container').innerHTML = message;
  });
}

function loadGrocery() {
    // fetches json list of groceries
    fetch('/grocery-list').then(response => handleErrors(response)).then((response) => 
        response.json()).then((groceries) => {
            
    const groceryListElement = document.getElementById('grocery-list-container'); 
    const groceryCompleteList = document.getElementById('grocery-complete-container');

    groceries.forEach((grocery) => {
        console.log(grocery.complete);
        if(grocery.complete === true) {
            groceryCompleteList.appendChild(createCompleteGrocery(grocery));
            return;
        }
        groceryListElement.appendChild(createGroceryElement(grocery));
    })
    }).catch(error => alert(error.message)); 
}

function createGroceryElement(grocery){
    const groceryCompleteList = document.getElementById('grocery-complete-container');

  const groceryElement = document.createElement('li');
  groceryElement.className = 'task';

  // If assigned email is empty then only show the item else show the item and the assigned email
  const titleElement = document.createElement('span');
  if(!grocery.email) {
    titleElement.innerText = grocery.item;
  } else {
    titleElement.innerText = grocery.item + " assigned to: " + grocery.email;
  }

  // only creates button for items assigned to user or no one
  if (isEditableGrocery(grocery)) {
    const deleteButtonElement = document.createElement('button');
    deleteButtonElement.innerText = 'Delete';
    deleteButtonElement.addEventListener('click', () => {
      deleteGrocery(grocery);
      // Remove the task from the DOM.
      groceryElement.remove();
    });

    const completeButtonElement = document.createElement('button');
    completeButtonElement.innerText = 'Complete';
    completeButtonElement.addEventListener('click', () => {
      groceryElement.className = 'taskComplete';
      completeButtonElement.remove();
      groceryElement.remove();
      completeGrocery(grocery);
      groceryCompleteList.appendChild(createCompleteGrocery(grocery));
    }); 

    if(isGroceryComplete(grocery)) {
      completeButtonElement.remove();
      groceryElement.appendChild(titleElement);
      groceryElement.appendChild(deleteButtonElement);
      return groceryElement;
    } 
    groceryElement.appendChild(titleElement);
    groceryElement.appendChild(deleteButtonElement);
    groceryElement.appendChild(completeButtonElement);
    return groceryElement;
    }
  groceryElement.appendChild(titleElement);
  return groceryElement;
    }

// adds items to the list of completed items
 function createCompleteGrocery(grocery) {
 const groceryElement = document.createElement('li');
  groceryElement.className = 'task';

  // If assigned email is empty then only show the item else show the item and the assigned email
  const titleElement = document.createElement('span');
  if(!grocery.email) {
    titleElement.innerText = grocery.item;
  } else {
    titleElement.innerText = grocery.item + " assigned to: " + grocery.email;
  }

  if (isEditableGrocery(grocery)) {
    const deleteButtonElement = document.createElement('button');
    deleteButtonElement.innerText = 'Delete';
    deleteButtonElement.addEventListener('click', () => {
      deleteGrocery(grocery);
      // Remove the task from the DOM.
      groceryElement.remove();
    });
    groceryElement.appendChild(titleElement);
    groceryElement.appendChild(deleteButtonElement);
    return groceryElement;

 }
    groceryElement.appendChild(titleElement);
    return groceryElement;
 }

/** Fetches tasks from the server and adds them to the DOM. */
function loadTasks() {
  fetch('/list-tasks').then(response => response.json()).then((tasks) => {
    const taskListElement = document.getElementById('task-list');
    tasks.forEach((task) => {
      taskListElement.appendChild(createTaskElement(task));
    })
  });
}
 
/** Creates an element that represents a task, including its delete button. */
function createTaskElement(task) {
  const currentUser = getCurrentUser();

  const taskElement = document.createElement('li');
  taskElement.className = 'task';
 
  const titleElement = document.createElement('span');
  titleElement.innerText = task.title;
      
  const deleteButtonElement = document.createElement('button');
  deleteButtonElement.innerText = 'Delete';
  deleteButtonElement.addEventListener('click', () => {
    deleteTask(task);
 
    // Remove the task from the DOM.
    taskElement.remove();
    
  });
  taskElement.appendChild(titleElement);
  taskElement.appendChild(deleteButtonElement);
  return taskElement;
}
 
/** Tells the server to delete the task. */
function deleteTask(task) {
  const params = new URLSearchParams();
  params.append('id', task.id);
  fetch('/delete-task', {method: 'POST', body: params});
}

/** Tells the server to delete the grocery. */
function deleteGrocery(grocery) {
  const params = new URLSearchParams();
  params.append('id', grocery.id);  
  fetch('/delete-grocery', {method: 'POST', body: params});
}


/** Tells the server mark the grocery complete. */
function completeGrocery(grocery) {
  const params = new URLSearchParams();
  params.append('id', grocery.id);  
  fetch('/complete-grocery', {method: 'POST', body: params});
}

/** Fetches tasks from the server and adds them to the DOM. */
function loadTasks() {
  fetch('/list-tasks').then(response => response.json()).then((tasks) => {
    const taskListElement = document.getElementById('task-list');
    tasks.forEach((task) => {
      taskListElement.appendChild(createTaskElement(task));
    })
  });
}


function isGroceryComplete(grocery) {
  return grocery.complete;
}

function isEditableGrocery(grocery) {
  return grocery.userMatch || !grocery.email; 
}

function insertCalendar() {
    const calElement = document.getElementById('cal-container');

    fetch('/calendar').then((response) => handleErrors(response)).then((response) => response.text()).then((calSrc) => {
        if(!calSrc.trim()) {
            return;
        }
        var calFrame = document.createElement('iframe');
        calFrame.setAttribute('src', calSrc);
        calFrame.setAttribute('style', 'border: 0'); 
        calFrame.setAttribute('width', '800'); 
        calFrame.setAttribute('height', '600'); 
        calFrame.setAttribute('frameborder', '0'); 
        calFrame.setAttribute('scrolling', 'no');
        calFrame.setAttribute('display', 'inline-block');
        calElement.appendChild(calFrame);

        document.getElementById('createCalendarButton').setAttribute("style","visibility:hidden");
        document.getElementById('deleteCalendarButton').setAttribute("style","visibility:visible");
    }).catch(error => alert(error.message));
}

function createCalendar() {
    fetch(new Request('/create-calendar', {method: 'POST'})).then((response) => handleErrors(response)).then(() => {
        insertCalendar();
    }).catch(error => alert(error.message));
}

function deleteCalendar() {
    fetch(new Request('/delete-calendar', {method: 'POST'})).then((response) => handleErrors(response)).then(() => {
        document.getElementById('cal-container').innerHTML = "";
        insertCalendar();
        document.getElementById('createCalendarButton').setAttribute("style","visibility:visible");
        document.getElementById('deleteCalendarButton').setAttribute("style","visibility:hidden");
    }).catch(error => alert(error.message));
}

function createGrocery() {
    const groceryForm = document.getElementById('groceryForm');
    const groceryItem = document.getElementById('groceryItemID');
    const assignGrocery = document.getElementById('assignGroceryID');

    groceryForm.append(groceryItem);
    groceryForm.append(assignGrocery);
    console.log(groceryForm.submit);
    fetch(new Request('/grocery-list', {method: 'POST', body: groceryForm.submit()})).then(() =>  
    handleErrors(response)).then((response) => {
        loadGrocery();
    });
}

/** Tells the server to delete the grocery. */
function deleteGrocery(grocery) {
  const params = new URLSearchParams();
  params.append('id', grocery.id);  
  fetch('/delete-grocery', {method: 'POST', body: params});
}

function handleErrors(response) {
    if (!response.ok) {
        return response.clone().text().then((errorMsg) => {
            console.log(errorMsg);
            throw new Error(errorMsg);
        });
    }
    return response;
}

/** Fetches photo urls from the server and adds them to the DOM. */
function loadPhotos() {
  fetch('/list-photos').then(response => response.json()).then((photos) => {
    const photoListElement = document.getElementById('photo-list');
    photos.forEach((photo) => {
      photoListElement.appendChild(createPhotoElement(photo));
      console.log("link is" + photo);
    })
  });
}

/** Creates an element that represents a photo url, including its delete button. */
function createPhotoElement(photo) {
  const photoElement = document.createElement('li');
  photoElement.className = 'photo';

  const titleElement = document.createElement('span');
  titleElement.innerText = photo.url;

  const deleteButtonElement = document.createElement('button');
  deleteButtonElement.innerText = 'Delete';
  deleteButtonElement.addEventListener('click', () => {
    deletePhoto(photo);

    // Remove the photo url from the DOM.
    photoElement.remove();
  });

  photoElement.appendChild(titleElement);
  photoElement.appendChild(deleteButtonElement);
  return photoElement;
}

/** Tells the server to delete the photo url. */
function deletePhoto(photo) {
  const params = new URLSearchParams();
  params.append('url', photo.url);
  fetch('/delete-photo', {method: 'POST', body: params});
}
