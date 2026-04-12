from django.urls import path
from . import views
urlpatterns = [
path('', views.home, name='home'),
path('about/', views.about, name='about'),
path('contact/', views.contact, name='contact'),
path('register/',views.register, name='register'),
path('thank/',views.thank, name='thank'),
path('loginpage/',views.loginpage, name='loginpage'),
path('dashboard/',views.dashboard, name='dashboard'),
path('logoutpage/',views.logoutpage, name='logoutpage'),
path('blog/<int:blog_id>/', views.blog_detail, name='blog_detail'),
path('add/', views.add_blog, name='add_blog'),
path('edit/<int:blog_id>/', views.edit_blog, name='edit_blog'),
path('delete/<int:blog_id>/', views.delete_blog, name='delete_blog'),
]