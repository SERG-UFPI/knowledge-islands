import { Container, Navbar, Nav, Button } from "react-bootstrap";
import { Link } from "react-router-dom";
import { useContext, useState } from "react";
import AuthContext from "./AuthContext";

const Layout = ({ children }) => {
    const { user, logout } = useContext(AuthContext);
    let path = "/home";
    if(user === null){
        path="/"
    }
    return (
        <>
            <Navbar bg="primary" variant="dark">
                <Navbar.Brand as={Link} style={{marginLeft: "3%"}} to={path} width="40" height="30" className="d-inline-block align-top">
                    Knowledge Islands</Navbar.Brand>
                <Nav className="ms-auto">
                    {user && <Nav.Link>{user?.email}</Nav.Link>}
                    {!user &&(
                        <Nav.Link as={Link} to="/login">
                            Login
                        </Nav.Link>
                    )}
                    {!user &&(
                        <Nav.Link as={Link} to="/signup">
                            Sign up
                        </Nav.Link>
                    )}
                </Nav>
                {user && (
                    <Button style={{marginRight: "1%"}} variant="outline-success" type="button" onClick={() => {logout()}}>
                        Logout
                    </Button>
                )}
            </Navbar>
            <Container>{children}</Container>
        </>
    );
};

export default Layout;