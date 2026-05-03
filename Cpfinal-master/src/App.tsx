import React from 'react';
import { BrowserRouter, Route, Routes } from 'react-router-dom';
import './App.css';
import AppHeader from './components/AppHeader';
import HomePage from './pages/HomePage';
import ProfilePage from './pages/ProfilePage';
import SearchPage from './pages/SearchPage';
import SignInPage from './pages/SignInPage';
import SignUpPage from './pages/SignUpPage';
import ForgotPasswordPage from './pages/ForgotPasswordPage';
import CreatePinPage from './pages/CreatePinPage';
import EditPinPage from './pages/EditPinPage';
import PinDetailPage from './pages/PinDetailPage';
import BoardDetailPage from './pages/BoardDetailPage';
import BusinessDetailPage from './pages/BusinessDetailPage';
import AdvertisingCampaignsPage from './pages/AdvertisingCampaignsPage';
import FollowersPage from './pages/FollowersPage';
import UserProfilePage from './pages/UserProfilePage';
import InvitationsPage from './pages/InvitationsPage';
import ShowcaseDetailPage from './pages/ShowcaseDetailPage';
import BusinessProfilesPage from './pages/BusinessProfilesPage';
import ProtectedRoute from './components/ProtectedRoute';
import ScrollToTop from './components/ScrollToTop';
import { AuthProvider } from './context/AuthContext';
import { UserProvider } from './context/UserContext';
import { InvitationProvider } from './context/InvitationContext';
import { CollaborationProvider } from './context/CollaborationContext';
import { SavedPinsProvider } from './context/SavedPinsContext';

function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <UserProvider>
          <InvitationProvider>
            <CollaborationProvider>
              <SavedPinsProvider>
                <div className="app-shell bg-body-tertiary min-vh-100">
                <ScrollToTop />
                <AppHeader />
                <Routes>
              <Route path="/signin" element={<SignInPage />} />
              <Route path="/signup" element={<SignUpPage />} />
              <Route path="/forgot-password" element={<ForgotPasswordPage />} />
              <Route element={<ProtectedRoute />}>
                <Route path="/" element={<HomePage />} />
                <Route path="/profile" element={<ProfilePage />} />
                <Route path="/search" element={<SearchPage />} />
                <Route path="/create" element={<CreatePinPage />} />
                <Route path="/pin/:pinId" element={<PinDetailPage />} />
                <Route path="/pin/:pinId/edit" element={<EditPinPage />} />
                <Route path="/board/:boardId" element={<BoardDetailPage />} />
                <Route path="/business/:businessId" element={<BusinessDetailPage />} />
                <Route path="/business/:businessId/showcase/:showcaseId" element={<ShowcaseDetailPage />} />
                <Route path="/campaigns" element={<AdvertisingCampaignsPage />} />
                <Route path="/followers/:type" element={<FollowersPage />} />
                <Route path="/user/:userId" element={<UserProfilePage />} />
                <Route path="/invitations" element={<InvitationsPage />} />
                <Route path="/invitations/:invitationId" element={<InvitationsPage />} />
                <Route path="/businesses" element={<BusinessProfilesPage />} />
              </Route>
              </Routes>
                </div>
              </SavedPinsProvider>
            </CollaborationProvider>
          </InvitationProvider>
        </UserProvider>
      </AuthProvider>
    </BrowserRouter>
  );
}

export default App;
