import axios from "axios";
import { createContext, useState } from "react";
import { Alert } from "react-bootstrap";
import { useNavigate } from "react-router-dom";

const AuthContext = createContext();

export const AuthContextProvider = ({children}) => {
    const [loginError, setLoginError] = useState(false);
    const[user, setUser] = useState(() => {
        let userProfile = localStorage.getItem("userProfile");
        if(userProfile){
            return JSON.parse(userProfile);
        }
        return null;
    });
    const navigate = useNavigate();
    const login = async (payload) => {
        try {
            let apiResponse = await axios.post(`${process.env.REACT_APP_API_URL}/auth/signin`, payload, {
                withCredentials: true,
            });
            localStorage.setItem("userProfile", JSON.stringify(apiResponse.data));
            setUser(apiResponse.data);
            navigate("/home");
            setLoginError(false);
        } catch (error) {
            setLoginError(true);            
        }
    };
    const logout = async () => {
        await axios.post(`${process.env.REACT_APP_API_URL}/auth/signout`, {withCredentials:true});
        localStorage.removeItem("userProfile");
        setUser(null);
        navigate("/login");
    }
    return (
        <>
        
            <AuthContext.Provider value={{ user, login, logout }}>
            {loginError?<Alert variant="danger">Erro ao realizar login</Alert>:
        null}
                {children}
            </AuthContext.Provider>
        </>
    );
};

export default AuthContext;