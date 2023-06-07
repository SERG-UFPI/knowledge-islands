import './App.css';
import { Route, Routes } from "react-router-dom";
import Layout from "./components/shared/Layout";
import Home from "./pages/Home"

function App() {
  // const requestBody = {
  //   "username": "OtavioCury",
  //   "password": "123456"
  // };
  // fetch("http://localhost:8080/auth/signin", {
  //   headers:{
  //     "Content-Type": "application/json"
  //   },
  //   method: "post",
  //   body: JSON.stringify(requestBody)
  // }).then(response => Promise.all([response.json(), response.headers]))
  // .then(([body, headers]) => {
  //   headers.forEach((element) => {
  //     console.log(element);
  //   })
  // });
  return (
    <div className="App">
      <>
        <Layout>
          <Routes>
            <Route path="/" element={<Home/>}></Route>
          </Routes>
        </Layout>
      </>
    </div>
  );
}

export default App;
