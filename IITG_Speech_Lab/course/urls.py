from django.contrib import admin
from django.urls import path
from . import views

app_name = 'course'

urlpatterns = [
    path('dashboard', views.dashboard, name='dashboard'),
    path('add_course', views.AddCourse, name='add_course'),
    path('<slug:cid>/', views.ViewCourse, name='view_course'),
]
