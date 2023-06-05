import logo from './logo.svg';
import './App.css';

function App() {
  const requestBody = {
    "username": "OtavioCury",
    "password": "123456"
  };
  fetch("http://localhost:8080/auth/signin", {
    headers:{
      "Content-Type": "application/json"
    },
    method: "post",
    body: JSON.stringify(requestBody)
  }).then(response => Promise.all([response.json(), response.headers]))
  return (
    <div className="App">
      
    </div>
  );
}

export default App;
