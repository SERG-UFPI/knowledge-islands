import { Route, Routes } from "react-router-dom";
import Layout from "./components/shared/Layout";
import SignupSuccess from "./pages/SignupSuccess";
import Home from "./pages/Home";
import Login from "./pages/Login";
import Index from "./pages/Index";
import GitRepositoryVersion from "./pages/GitRepositoryVersion"
import { AuthContextProvider } from "./components/shared/AuthContext";
import ProtectedRoute from './components/shared/ProtectedRoute';
import SignUp from "./pages/SignUp";
import VerifyRegistration from "./pages/VerifyRegistration";

function App() {
  
  return (
    <div className="App">
      <>
        <AuthContextProvider>
        <Layout>
          <Routes>
            <Route path="/verify" element={
              <ProtectedRoute accessBy="non-authenticated">
                <VerifyRegistration/>
              </ProtectedRoute>  
            }>
            </Route>
            <Route path="/home" element={
              <ProtectedRoute accessBy="authenticated">
                <Home/>
              </ProtectedRoute>
            }></Route>
            <Route path="/git-repository-version" element={
              <ProtectedRoute accessBy="authenticated">
                <GitRepositoryVersion/>
              </ProtectedRoute>
            }>
            </Route>
            <Route path="/login" element={
              <ProtectedRoute accessBy="non-authenticated">
                <Login/>
              </ProtectedRoute>
            }></Route>
            <Route path="/signup" element={
              <ProtectedRoute accessBy="non-authenticated">
                <SignUp/>
              </ProtectedRoute>
            }></Route>
            <Route path="/signup-success" element={
              <ProtectedRoute accessBy="non-authenticated">
                <SignupSuccess/>
              </ProtectedRoute>
            }></Route>
            <Route path="/" element={
              <ProtectedRoute accessBy="non-authenticated">
                <Index/>
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