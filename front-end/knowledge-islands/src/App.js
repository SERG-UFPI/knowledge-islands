import { Route, Routes } from "react-router-dom";
import Layout from "./components/shared/Layout";
import Home from "./pages/Home";
import Login from "./pages/Login";
import { AuthContextProvider } from "./components/shared/AuthContext";
import ProtectedRoute from './components/shared/ProtectedRoute';


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
        <AuthContextProvider>
        <Layout>
          <Routes>
            <Route path="/" element={
              <ProtectedRoute accessBy="authenticated">
                <Home/>
              </ProtectedRoute>
            }></Route>
            <Route path="/login" element={
              <ProtectedRoute accessBy="non-authenticated">
                <Login/>
              </ProtectedRoute>
            }></Route>
          </Routes>
        </Layout>
        </AuthContextProvider>
      </>
    </div>
  );
}

export default App;
